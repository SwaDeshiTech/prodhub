package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingProgressDTO {
    private String serviceId;
    private String serviceName;
    private int totalSteps;
    private int completedSteps;
    private int progressPercentage;
    private List<OnboardingStep> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OnboardingStep {
        private String name;
        private boolean completed;
        private String description;
        private int sequence;
        private String status; // COMPLETED, PENDING, OPTIONAL
    }
}
