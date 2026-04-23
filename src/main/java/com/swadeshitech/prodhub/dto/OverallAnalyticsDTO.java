package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverallAnalyticsDTO {
    private int totalDeployments;
    private int successfulDeployments;
    private int failedDeployments;
    private int pendingDeployments;
    private int totalBuilds;
    private int successfulBuilds;
    private int failedBuilds;
    private int pendingBuilds;
    private int totalPipelines;
    private int successfulPipelines;
    private int failedPipelines;
    private int pendingPipelines;
}
