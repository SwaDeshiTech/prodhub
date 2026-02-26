package com.swadeshitech.prodhub.enums;


public enum PipelineTemplateType {
    BUILD("Build"),
    DEPLOYMENT("Deployment");

    private final String value;

    PipelineTemplateType(String value) {
        this.value = value;
    }

    public String getMessage() {
        return this.value;
    }

    public static PipelineTemplateType fromValue(String value) {
        for (PipelineTemplateType pipelineTemplate : PipelineTemplateType.values()) {
            if(pipelineTemplate.value.equalsIgnoreCase(value)) {
                return pipelineTemplate;
            }
        }
        return null;
    }
}
