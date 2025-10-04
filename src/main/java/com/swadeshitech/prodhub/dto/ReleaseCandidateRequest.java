package com.swadeshitech.prodhub.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseCandidateRequest {
    private String serviceName;
    private String buildProfile;
    private String releaseCandidateStatus;
    private Map<String, String> metadata;
    private String ephemeralEnvironmentName;
    private String certifiedBy;
}
