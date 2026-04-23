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
public class BuildAnalyticsDTO {
    private List<BuildSummary> recentBuilds;
    private List<DailyStats> buildStatsLast7Days;
    private BuildStats overallStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BuildSummary {
        private String buildId;
        private String buildRefId;
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
    public static class BuildStats {
        private int totalBuilds;
        private int successfulBuilds;
        private int failedBuilds;
        private int pendingBuilds;
    }
}
