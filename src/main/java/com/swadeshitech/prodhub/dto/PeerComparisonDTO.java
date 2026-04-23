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
public class PeerComparisonDTO {
    private String peerId;
    private String peerName;
    private String peerType; // "USER", "TEAM", "DEPARTMENT"
    private PeerMetrics metrics;
    private int rank;
    private double percentile;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeerMetrics {
        private int totalDeployments;
        private int successfulDeployments;
        private int failedDeployments;
        private double successRate;
        private int totalBuilds;
        private int successfulBuilds;
        private int failedBuilds;
        private double buildSuccessRate;
        private double avgDeploymentTimeMinutes;
        private double avgBuildTimeMinutes;
        private int deploymentsLast7Days;
        private int deploymentsLast30Days;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamComparison {
        private String teamId;
        private String teamName;
        private List<PeerComparisonDTO> teamMembers;
        private PeerMetrics teamMetrics;
        private int teamRank;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DepartmentComparison {
        private String departmentId;
        private String departmentName;
        private List<TeamComparison> teams;
        private PeerMetrics departmentMetrics;
        private int departmentRank;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeploymentTimeComparison {
        private String peerId;
        private String peerName;
        private List<DeploymentTimeEntry> deploymentTimes;
        private double avgDeploymentTimeMinutes;
        private double medianDeploymentTimeMinutes;
        private double minDeploymentTimeMinutes;
        private double maxDeploymentTimeMinutes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeploymentTimeEntry {
        private String deploymentId;
        private String serviceName;
        private long startTime;
        private long endTime;
        private double durationMinutes;
        private String status;
    }
}
