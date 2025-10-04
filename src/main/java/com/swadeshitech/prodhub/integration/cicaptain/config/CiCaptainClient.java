package com.swadeshitech.prodhub.integration.cicaptain.config;

import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildStatusResponse;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerRequest;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CiCaptainClient {

    private final WebClient webClient;

    // Inject the configured bean
    public CiCaptainClient(WebClient ciCaptainWebClient) {
        this.webClient = ciCaptainWebClient;
    }

    @Value("${cicaptain.uri}")
    String cicaptainBaseURL;

    @Value("${cicaptain.triggerBuildPath}")
    String triggerBuildPath;

    @Value("${cicaptain.buildStatus}")
    String buildStatusPath;

    /**
     * Triggers a CI Captain job using a reactive, non-blocking POST.
     *
     * @param providerId  e.g., "68d8f3b62d7f621b3ab8da6d"
     * @param jobName     e.g., "prodhub_build"
     * @param request     BuildTriggerRequest payload
     * @return            Mono<CiCaptainResponse>
     */
    public Mono<BuildTriggerResponse> triggerBuild(
            String providerId,
            String jobName,
            BuildTriggerRequest request
    ) {
        return webClient.post()
                .uri(cicaptainBaseURL + triggerBuildPath,
                        providerId, jobName)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("CI Captain error")
                                .map(msg -> new RuntimeException("CI Captain HTTP " + resp.statusCode() + " - " + msg)))
                .bodyToMono(BuildTriggerResponse.class);
    }

    public Mono<BuildStatusResponse> getBuildStatus(String baseUrl, String buildId) {
        return webClient.get()
                .uri(cicaptainBaseURL + buildStatusPath, buildId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BuildStatusResponse.class);
    }
}
