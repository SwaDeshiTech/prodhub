package com.swadeshitech.prodhub.dto;

import lombok.Data;

import java.util.List;

@Data
public class PipelineTemplateRequest {
    private String name;
    private String version;
    private List<PipelineTemplateStageRequest> stages;

    @Data
    public static class PipelineTemplateStageRequest {
        private String name;
        private String templateName;
        private int order;
        private boolean stopOnFailure;
        private String version;
    }
}
