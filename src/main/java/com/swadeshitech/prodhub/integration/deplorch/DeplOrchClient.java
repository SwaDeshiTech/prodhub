package com.swadeshitech.prodhub.integration.deplorch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class DeplOrchClient {

    private final WebClient webClient;

    // Inject the configured bean
    public DeplOrchClient(WebClient ciCaptainWebClient) {
        this.webClient = ciCaptainWebClient;
    }

    @Value("${deplorch.uri}")
    String deplorchBaseURL;

    @Value("${deplorch.initiateDeployment}")
    String initiateDeployment;

    public Mono<DeploymentResponse> triggerDeployment(
            DeploymentRequest request
    ) {
        return webClient.post()
                .uri(deplorchBaseURL + initiateDeployment)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("Deployment Orchestrator error")
                                .map(msg -> new RuntimeException("Deployment Orchestrator HTTP " + resp.statusCode() + " - " + msg)))
                .bodyToMono(DeploymentResponse.class);
    }
}
