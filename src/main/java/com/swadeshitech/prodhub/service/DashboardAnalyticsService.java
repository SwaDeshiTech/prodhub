package com.swadeshitech.prodhub.service;

import com.swadeshitech.prodhub.dto.*;

import java.util.List;

public interface DashboardAnalyticsService {
    DeploymentAnalyticsDTO getDeploymentAnalytics(String userId);
    BuildAnalyticsDTO getBuildAnalytics(String userId);
    OverallAnalyticsDTO getOverallAnalytics(String userId);
    List<PeerComparisonDTO> getTeamPeerComparison(String teamId);
    List<PeerComparisonDTO> getDepartmentPeerComparison(String departmentId);
    PeerComparisonDTO.DeploymentTimeComparison getDeploymentTimeComparison(String userId);
    List<PeerComparisonDTO.DeploymentTimeComparison> getTeamDeploymentTimeComparison(String teamId);
    List<PeerComparisonDTO.DeploymentTimeComparison> getDepartmentDeploymentTimeComparison(String departmentId);
}
