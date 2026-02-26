package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TemplateRequest {
    private String id;
    private String templateName;
    private String version;
    private String description;
    private List<TemplateStepRequest> stepRequests;

    @Data
    @Builder
    public static class TemplateStepRequest {
        private int order;
        private String stepName;
        private ChartDetailsRequest chartDetails;
        private boolean wait;
        private int timeoutSeconds;
        private boolean skipStep;
        private Map<String, TemplateStepRequestParam> params;

        @Data
        @Builder
        public static class ChartDetailsRequest {
            private String repository;
            private String chartName;
            private String version;
            private String chartLink;
        }

        @Data
        @Builder
        public static class TemplateStepRequestParam {
            private String affectedKey;
            private String displayName;
        }
    }
}
