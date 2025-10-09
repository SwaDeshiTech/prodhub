package com.swadeshitech.prodhub.integration.cicaptain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class BuildTriggerRequest {
    private Map<String, String> parameters;
    @JsonProperty("triggered_by")
    private String triggeredBy;
    private String refId;
}