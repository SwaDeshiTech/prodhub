package com.swadeshitech.prodhub.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class ReleaseCandidateRequest {
    private String serviceName;
    private String buildProfile;
    private String releaseCandidateStatus;
    private Map<String, String> metadata;
    private String ephemeralEnvironmentName;
    private String certifiedBy;
}
