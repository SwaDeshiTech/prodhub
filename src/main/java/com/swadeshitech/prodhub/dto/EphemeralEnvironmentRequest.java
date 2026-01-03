package com.swadeshitech.prodhub.dto;

import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class EphemeralEnvironmentRequest {
    private String name;
    private int expiryDuration;
    private List<EphemeralEnvironmentRequestApplications> profiles;
    private Set<String> sharedWith;

    @Data
    public static class EphemeralEnvironmentRequestApplications {
        private String applicationId;
        private String buildProfileId;
        private String deploymentProfileId;
        private String actionType;
    }
}
