package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
public class DeploymentTemplateRequest {
    private String id;
    private String templateName;
    private String version;
    private String description;
    List<DeploymentTemplateStepRequest> stepRequests;

    @Data
    @Builder
    public static class DeploymentTemplateStepRequest {
        private int order;
        private String stepName;
        private ChartDetailsRequest chartDetails;
        private boolean wait;
        private int timeoutSeconds;
        private List<String> params;

        @Data
        @Builder
        public static class ChartDetailsRequest {
            private String repository;
            private String chartName;
            private String version;
            private String chartLink;
        }
    }
}
