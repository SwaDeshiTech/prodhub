package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class DeploymentSetRequest {
    private String applicationId;
    private String deploymentProfileId;
    private String releaseCandidateId;
}
