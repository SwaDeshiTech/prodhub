package com.swadeshitech.prodhub.integration.cicaptain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuildTriggerRequest {
    @JsonProperty("providerId")
    private String providerId;

    @JsonProperty("pipelineExecutionId")
    private String pipelineExecutionId;

    @JsonProperty("stageExecutionId")
    private String stageExecutionId;

    @JsonProperty("triggered_by")
    private String triggeredBy;
}