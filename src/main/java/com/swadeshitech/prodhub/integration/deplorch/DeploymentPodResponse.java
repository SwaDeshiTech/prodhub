package com.swadeshitech.prodhub.integration.deplorch;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeploymentPodResponse {
    private String clusterId;
    private String count;
    private String namespace;
    private List<PodResponse> pods;

    @Data
    @Builder
    public static class PodResponse {
        private String namespace;
        private String name;
        private String status;
        private String ready;
        private String age;
        private String ip;
        private String node;
        private int restarts;
    }
}
