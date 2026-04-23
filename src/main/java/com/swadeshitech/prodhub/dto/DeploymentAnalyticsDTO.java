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
public class DeploymentAnalyticsDTO {
    private List<DeploymentSummary> recentDeployments;
    private List<DailyStats> deploymentStatsLast7Days;
    private DeploymentStats overallStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeploymentSummary {
        private String deploymentId;
        private String deploymentSetName;
        private String serviceName;
        private String status;
        private String initiatedBy;
        private String createdTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyStats {
        private String date;
        private int count;
        private int successCount;
        private int failureCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeploymentStats {
        private int totalDeployments;
        private int successfulDeployments;
        private int failedDeployments;
        private int pendingDeployments;
    }
}
