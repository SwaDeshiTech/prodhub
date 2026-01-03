package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.swadeshitech.prodhub.enums.EphemeralEnvrionmentStatus;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class EphemeralEnvironmentResponse extends BaseResponse {
    private String id;
    private String name;
    private String owner;
    private EphemeralEnvrionmentStatus status;
    private Set<DropdownDTO> applications;
    private Set<DropdownDTO> sharedWith;
    private LocalDateTime expiryOn;
    private LocalDateTime createdTime;
    private List<EphemeralEnvironmentProfileResponse> profiles;

    @Data
    @Builder
    public static class EphemeralEnvironmentProfileResponse {
        private String applicationName;
        private String buildProfileName;
        private String deploymentProfileName;
        private String deploymentProfileId;
        private String buildProfileId;
    }
}
