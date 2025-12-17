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

    public DeplOrchClient(WebClient ciCaptainWebClient) {
        this.webClient = ciCaptainWebClient;
    }

    @Value("${deplorch.uri}")
    String deplorchBaseURL;

    @Value("${deplorch.initiateDeployment}")
    String initiateDeployment;

    @Value("${deplorch.deployedPodDetails}")
    String deployedPodDetails;

    public Mono<DeploymentResponse> triggerDeployment(String deploymentID) {
        return webClient.post()
                .uri(deplorchBaseURL + initiateDeployment + "/" + deploymentID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("Deployment Orchestrator error")
                                .map(msg -> new RuntimeException("Deployment Orchestrator HTTP " + resp.statusCode() + " - " + msg)))
                .bodyToMono(DeploymentResponse.class);
    }

    public Mono<DeploymentPodResponse> getDeployedPodDetails(String k8sClusterId, String namespace) {
        return webClient.get()
                .uri(deplorchBaseURL + deployedPodDetails + "/" + k8sClusterId + "/" + namespace)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("Deployment Orchestrator error")
                                .map(msg -> new RuntimeException("Deployment Orchestrator HTTP " + resp.statusCode() + " - " + msg)))
                .bodyToMono(DeploymentPodResponse.class);
    }
}
