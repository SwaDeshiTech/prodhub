package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
public class DeploymentTemplateResponse extends BaseResponse {
    private String id;
    private String templateName;
    private String version;
    private String description;
    List<DeploymentTemplateStepResponse> stepResponses;

    @Data
    @Builder
    public static class DeploymentTemplateStepResponse {
        private int order;
        private String stepName;
        private ChartDetailsResponse chartDetails;
        private boolean wait;
        private int timeoutSeconds;
        private Map<String, Object> values;
        private List<String> params;

        @Data
        @Builder
        public static class ChartDetailsResponse {
            private String repository;
            private String chartName;
            private String version;
            private String chartLink;
        }
    }
}
