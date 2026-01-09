package com.swadeshitech.prodhub.dto;

import lombok.Data;

import java.util.List;

@Data
public class EphemeralEnvironmentBuildAndDeployRequest {

    String ephemeralEnvironmentId;
    List<EphemeralEnvironmentServiceProfiles> profiles;
    String userId;

    @Data
    public static class EphemeralEnvironmentServiceProfiles {
        private String buildProfileId;
        private String deploymentProfileId;
    }
}
