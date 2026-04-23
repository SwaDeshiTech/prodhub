package com.swadeshitech.prodhub.service.impl;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.enums.DeploymentStatus;
import com.swadeshitech.prodhub.enums.DeploymentSetStatus;
import com.swadeshitech.prodhub.enums.PipelineStatus;
import com.swadeshitech.prodhub.enums.ReleaseCandidateStatus;
import com.swadeshitech.prodhub.repository.DeploymentRepository;
import com.swadeshitech.prodhub.repository.DeploymentSetRepository;
import com.swadeshitech.prodhub.repository.PipelineExecutionRepository;
import com.swadeshitech.prodhub.repository.ReleaseCandidateRepository;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.repository.TeamRepository;
import com.swadeshitech.prodhub.repository.DepartmentRepository;
import com.swadeshitech.prodhub.service.DashboardAnalyticsService;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DashboardAnalyticsServiceImpl implements DashboardAnalyticsService {

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Autowired
    private DeploymentSetRepository deploymentSetRepository;

    @Autowired
    private PipelineExecutionRepository pipelineExecutionRepository;

    @Autowired
    private ReleaseCandidateRepository releaseCandidateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public DeploymentAnalyticsDTO getDeploymentAnalytics(String userId) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Fetch recent deployments
        List<Deployment> recentDeployments = deploymentRepository.findTop5ByOrderByCreatedTimeDesc();
        List<DeploymentAnalyticsDTO.DeploymentSummary> deploymentSummaries = recentDeployments.stream()
                .map(deployment -> DeploymentAnalyticsDTO.DeploymentSummary.builder()
                        .deploymentId(deployment.getId())
                        .deploymentSetName(deployment.getDeploymentSet() != null ? deployment.getDeploymentSet().getUuid() : "N/A")
                        .serviceName(deployment.getApplication() != null ? deployment.getApplication().getName() : "N/A")
                        .status(deployment.getStatus() != null ? deployment.getStatus().name() : "UNKNOWN")
                        .initiatedBy(deployment.getCreatedBy())
                        .createdTime(deployment.getCreatedTime() != null ? deployment.getCreatedTime().toString() : "N/A")
                        .build())
                .collect(Collectors.toList());

        // Fetch deployment stats for last 7 days
        List<DeploymentAnalyticsDTO.DailyStats> deploymentStats = getDailyStatsForDeployments(sevenDaysAgo, formatter);

        // Calculate deployment stats
        DeploymentAnalyticsDTO.DeploymentStats deploymentStatsOverall = calculateDeploymentStats();

        return DeploymentAnalyticsDTO.builder()
                .recentDeployments(deploymentSummaries)
                .deploymentStatsLast7Days(deploymentStats)
                .overallStats(deploymentStatsOverall)
                .build();
    }

    @Override
    public BuildAnalyticsDTO getBuildAnalytics(String userId) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Fetch recent builds (release candidates)
        List<ReleaseCandidate> recentBuilds = releaseCandidateRepository.findTop5ByOrderByCreatedTimeDesc();
        List<BuildAnalyticsDTO.BuildSummary> buildSummaries = recentBuilds.stream()
                .map(build -> BuildAnalyticsDTO.BuildSummary.builder()
                        .buildId(build.getId())
                        .buildRefId(build.getBuildRefId())
                        .serviceName(build.getService() != null ? build.getService().getName() : "N/A")
                        .status(build.getStatus() != null ? build.getStatus().name() : "UNKNOWN")
                        .initiatedBy(build.getCreatedBy())
                        .createdTime(build.getCreatedTime() != null ? build.getCreatedTime().toString() : "N/A")
                        .build())
                .collect(Collectors.toList());

        // Fetch build stats for last 7 days
        List<BuildAnalyticsDTO.DailyStats> buildStats = getDailyStatsForBuilds(sevenDaysAgo, formatter);

        // Calculate build stats
        BuildAnalyticsDTO.BuildStats buildStatsOverall = calculateBuildStats();

        return BuildAnalyticsDTO.builder()
                .recentBuilds(buildSummaries)
                .buildStatsLast7Days(buildStats)
                .overallStats(buildStatsOverall)
                .build();
    }

    @Override
    public OverallAnalyticsDTO getOverallAnalytics(String userId) {
        return OverallAnalyticsDTO.builder()
                .totalDeployments(calculateDeploymentStats().getTotalDeployments())
                .successfulDeployments(calculateDeploymentStats().getSuccessfulDeployments())
                .failedDeployments(calculateDeploymentStats().getFailedDeployments())
                .pendingDeployments(calculateDeploymentStats().getPendingDeployments())
                .totalBuilds(calculateBuildStats().getTotalBuilds())
                .successfulBuilds(calculateBuildStats().getSuccessfulBuilds())
                .failedBuilds(calculateBuildStats().getFailedBuilds())
                .pendingBuilds(calculateBuildStats().getPendingBuilds())
                .totalPipelines(calculatePipelineStats().getTotalPipelines())
                .successfulPipelines(calculatePipelineStats().getSuccessfulPipelines())
                .failedPipelines(calculatePipelineStats().getFailedPipelines())
                .pendingPipelines(calculatePipelineStats().getPendingPipelines())
                .build();
    }

    private List<DeploymentAnalyticsDTO.DailyStats> getDailyStatsForDeployments(LocalDate sevenDaysAgo, DateTimeFormatter formatter) {
        List<DeploymentAnalyticsDTO.DailyStats> stats = new ArrayList<>();
        List<DeploymentSet> allDeploymentSets = deploymentSetRepository.findAll();

        for (int i = 0; i < 7; i++) {
            LocalDate date = sevenDaysAgo.plusDays(i);
            String dateStr = date.format(formatter);

            int totalCount = 0;
            int successCount = 0;
            int failureCount = 0;

            for (DeploymentSet deploymentSet : allDeploymentSets) {
                if (deploymentSet.getCreatedTime() != null) {
                    LocalDate createdDate = deploymentSet.getCreatedTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    
                    if (createdDate.equals(date)) {
                        totalCount++;
                        if (deploymentSet.getStatus() == DeploymentSetStatus.COMPLETED) {
                            successCount++;
                        } else if (deploymentSet.getStatus() == DeploymentSetStatus.FAILED) {
                            failureCount++;
                        }
                    }
                }
            }

            stats.add(DeploymentAnalyticsDTO.DailyStats.builder()
                    .date(dateStr)
                    .count(totalCount)
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .build());
        }

        return stats;
    }

    private List<BuildAnalyticsDTO.DailyStats> getDailyStatsForBuilds(LocalDate sevenDaysAgo, DateTimeFormatter formatter) {
        List<BuildAnalyticsDTO.DailyStats> stats = new ArrayList<>();
        List<ReleaseCandidate> allBuilds = releaseCandidateRepository.findAll();

        for (int i = 0; i < 7; i++) {
            LocalDate date = sevenDaysAgo.plusDays(i);
            String dateStr = date.format(formatter);

            int totalCount = 0;
            int successCount = 0;
            int failureCount = 0;

            for (ReleaseCandidate build : allBuilds) {
                if (build.getCreatedTime() != null) {
                    LocalDate createdDate = build.getCreatedTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    
                    if (createdDate.equals(date)) {
                        totalCount++;
                        if (build.getStatus() == ReleaseCandidateStatus.CERTIFIED) {
                            successCount++;
                        } else if (build.getStatus() == ReleaseCandidateStatus.FAILED) {
                            failureCount++;
                        }
                    }
                }
            }

            stats.add(BuildAnalyticsDTO.DailyStats.builder()
                    .date(dateStr)
                    .count(totalCount)
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .build());
        }

        return stats;
    }

    private DeploymentAnalyticsDTO.DeploymentStats calculateDeploymentStats() {
        List<Deployment> allDeployments = deploymentRepository.findAll();

        int totalDeployments = allDeployments.size();
        int successfulDeployments = (int) allDeployments.stream()
                .filter(d -> d.getStatus() == DeploymentStatus.SUCCESS)
                .count();
        int failedDeployments = (int) allDeployments.stream()
                .filter(d -> d.getStatus() == DeploymentStatus.FAILED)
                .count();
        int pendingDeployments = (int) allDeployments.stream()
                .filter(d -> d.getStatus() == DeploymentStatus.PENDING)
                .count();

        return DeploymentAnalyticsDTO.DeploymentStats.builder()
                .totalDeployments(totalDeployments)
                .successfulDeployments(successfulDeployments)
                .failedDeployments(failedDeployments)
                .pendingDeployments(pendingDeployments)
                .build();
    }

    private BuildAnalyticsDTO.BuildStats calculateBuildStats() {
        List<ReleaseCandidate> allBuilds = releaseCandidateRepository.findAll();

        int totalBuilds = allBuilds.size();
        int successfulBuilds = (int) allBuilds.stream()
                .filter(b -> b.getStatus() == ReleaseCandidateStatus.CERTIFIED)
                .count();
        int failedBuilds = (int) allBuilds.stream()
                .filter(b -> b.getStatus() == ReleaseCandidateStatus.FAILED)
                .count();
        int pendingBuilds = (int) allBuilds.stream()
                .filter(b -> b.getStatus() == ReleaseCandidateStatus.PENDING)
                .count();

        return BuildAnalyticsDTO.BuildStats.builder()
                .totalBuilds(totalBuilds)
                .successfulBuilds(successfulBuilds)
                .failedBuilds(failedBuilds)
                .pendingBuilds(pendingBuilds)
                .build();
    }

    private OverallAnalyticsDTO calculatePipelineStats() {
        List<PipelineExecution> allPipelines = pipelineExecutionRepository.findAll();

        int totalPipelines = allPipelines.size();
        int successfulPipelines = (int) allPipelines.stream()
                .filter(p -> p.getStatus() == PipelineStatus.SUCCESS)
                .count();
        int failedPipelines = (int) allPipelines.stream()
                .filter(p -> p.getStatus() == PipelineStatus.FAILED)
                .count();
        int pendingPipelines = (int) allPipelines.stream()
                .filter(p -> p.getStatus() == PipelineStatus.IN_PROGRESS)
                .count();

        return OverallAnalyticsDTO.builder()
                .totalPipelines(totalPipelines)
                .successfulPipelines(successfulPipelines)
                .failedPipelines(failedPipelines)
                .pendingPipelines(pendingPipelines)
                .build();
    }

    @Override
    public List<PeerComparisonDTO> getTeamPeerComparison(String teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return Collections.emptyList();
        }

        List<PeerComparisonDTO> comparisons = new ArrayList<>();
        Set<User> members = team.getEmployees();
        if (members != null) {
            for (User user : members) {
                PeerComparisonDTO.PeerMetrics metrics = calculateUserMetrics(user.getId());
                comparisons.add(PeerComparisonDTO.builder()
                        .peerId(user.getId())
                        .peerName(user.getName())
                        .peerType("USER")
                        .metrics(metrics)
                        .build());
            }
        }

        // Calculate ranks and percentiles
        calculateRanksAndPercentiles(comparisons);

        return comparisons.stream()
                .sorted(Comparator.comparingInt(PeerComparisonDTO::getRank))
                .collect(Collectors.toList());
    }

    @Override
    public List<PeerComparisonDTO> getDepartmentPeerComparison(String departmentId) {
        Department department = departmentRepository.findById(departmentId).orElse(null);
        if (department == null) {
            return Collections.emptyList();
        }

        List<PeerComparisonDTO> comparisons = new ArrayList<>();
        Set<Team> teams = department.getTeams();
        if (teams != null) {
            for (Team team : teams) {
                PeerComparisonDTO.PeerMetrics metrics = calculateTeamMetrics(team.getId());
                comparisons.add(PeerComparisonDTO.builder()
                        .peerId(team.getId())
                        .peerName(team.getName())
                        .peerType("TEAM")
                        .metrics(metrics)
                        .build());
            }
        }

        // Calculate ranks and percentiles
        calculateRanksAndPercentiles(comparisons);

        return comparisons.stream()
                .sorted(Comparator.comparingInt(PeerComparisonDTO::getRank))
                .collect(Collectors.toList());
    }

    @Override
    public PeerComparisonDTO.DeploymentTimeComparison getDeploymentTimeComparison(String userId) {
        List<DeploymentSet> userDeploymentSets = deploymentSetRepository.findAll().stream()
                .filter(ds -> ds.getReleaseCandidate() != null && 
                        ds.getReleaseCandidate().getInitiatedBy() != null &&
                        ds.getReleaseCandidate().getInitiatedBy().getId().equals(userId))
                .collect(Collectors.toList());

        List<PeerComparisonDTO.DeploymentTimeEntry> timeEntries = new ArrayList<>();
        for (DeploymentSet ds : userDeploymentSets) {
            if (ds.getCreatedTime() != null && ds.getUpdatedTime() != null) {
                long durationMinutes = (ds.getUpdatedTime().getTime() - ds.getCreatedTime().getTime()) / (1000 * 60);
                timeEntries.add(PeerComparisonDTO.DeploymentTimeEntry.builder()
                        .deploymentId(ds.getId())
                        .serviceName(ds.getApplication() != null ? ds.getApplication().getName() : "N/A")
                        .startTime(ds.getCreatedTime().getTime())
                        .endTime(ds.getUpdatedTime().getTime())
                        .durationMinutes(durationMinutes)
                        .status(ds.getStatus() != null ? ds.getStatus().name() : "UNKNOWN")
                        .build());
            }
        }

        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getName() : "Unknown";

        return calculateTimeComparison(userId, userName, timeEntries);
    }

    @Override
    public List<PeerComparisonDTO.DeploymentTimeComparison> getTeamDeploymentTimeComparison(String teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return Collections.emptyList();
        }

        List<PeerComparisonDTO.DeploymentTimeComparison> comparisons = new ArrayList<>();
        Set<User> members = team.getEmployees();
        if (members != null) {
            for (User user : members) {
                comparisons.add(getDeploymentTimeComparison(user.getId()));
            }
        }

        return comparisons;
    }

    @Override
    public List<PeerComparisonDTO.DeploymentTimeComparison> getDepartmentDeploymentTimeComparison(String departmentId) {
        Department department = departmentRepository.findById(departmentId).orElse(null);
        if (department == null) {
            return Collections.emptyList();
        }

        List<PeerComparisonDTO.DeploymentTimeComparison> comparisons = new ArrayList<>();
        Set<Team> teams = department.getTeams();
        if (teams != null) {
            for (Team team : teams) {
                comparisons.addAll(getTeamDeploymentTimeComparison(team.getId()));
            }
        }

        return comparisons;
    }

    private PeerComparisonDTO.PeerMetrics calculateUserMetrics(String userId) {
        List<DeploymentSet> userDeploymentSets = deploymentSetRepository.findAll().stream()
                .filter(ds -> ds.getReleaseCandidate() != null && 
                        ds.getReleaseCandidate().getInitiatedBy() != null &&
                        ds.getReleaseCandidate().getInitiatedBy().getId().equals(userId))
                .collect(Collectors.toList());

        List<ReleaseCandidate> userBuilds = releaseCandidateRepository.findAll().stream()
                .filter(rc -> rc.getInitiatedBy() != null && rc.getInitiatedBy().getId().equals(userId))
                .collect(Collectors.toList());

        int totalDeployments = userDeploymentSets.size();
        int successfulDeployments = (int) userDeploymentSets.stream()
                .filter(ds -> ds.getStatus() == DeploymentSetStatus.COMPLETED)
                .count();
        int failedDeployments = (int) userDeploymentSets.stream()
                .filter(ds -> ds.getStatus() == DeploymentSetStatus.FAILED)
                .count();

        int totalBuilds = userBuilds.size();
        int successfulBuilds = (int) userBuilds.stream()
                .filter(b -> b.getStatus() == ReleaseCandidateStatus.CERTIFIED)
                .count();
        int failedBuilds = (int) userBuilds.stream()
                .filter(b -> b.getStatus() == ReleaseCandidateStatus.FAILED)
                .count();

        double successRate = totalDeployments > 0 ? (double) successfulDeployments / totalDeployments * 100 : 0;
        double buildSuccessRate = totalBuilds > 0 ? (double) successfulBuilds / totalBuilds * 100 : 0;

        double avgDeploymentTime = calculateAverageDeploymentTime(userDeploymentSets);
        double avgBuildTime = calculateAverageBuildTime(userBuilds);

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        int deploymentsLast7Days = (int) userDeploymentSets.stream()
                .filter(ds -> ds.getCreatedTime() != null)
                .filter(ds -> ds.getCreatedTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate().isAfter(sevenDaysAgo.minusDays(1)))
                .count();

        int deploymentsLast30Days = (int) userDeploymentSets.stream()
                .filter(ds -> ds.getCreatedTime() != null)
                .filter(ds -> ds.getCreatedTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate().isAfter(thirtyDaysAgo.minusDays(1)))
                .count();

        return PeerComparisonDTO.PeerMetrics.builder()
                .totalDeployments(totalDeployments)
                .successfulDeployments(successfulDeployments)
                .failedDeployments(failedDeployments)
                .successRate(successRate)
                .totalBuilds(totalBuilds)
                .successfulBuilds(successfulBuilds)
                .failedBuilds(failedBuilds)
                .buildSuccessRate(buildSuccessRate)
                .avgDeploymentTimeMinutes(avgDeploymentTime)
                .avgBuildTimeMinutes(avgBuildTime)
                .deploymentsLast7Days(deploymentsLast7Days)
                .deploymentsLast30Days(deploymentsLast30Days)
                .build();
    }

    private PeerComparisonDTO.PeerMetrics calculateTeamMetrics(String teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return PeerComparisonDTO.PeerMetrics.builder().build();
        }

        List<PeerComparisonDTO.PeerMetrics> memberMetrics = new ArrayList<>();
        Set<User> members = team.getEmployees();
        if (members != null) {
            for (User user : members) {
                memberMetrics.add(calculateUserMetrics(user.getId()));
            }
        }

        int totalDeployments = memberMetrics.stream().mapToInt(PeerComparisonDTO.PeerMetrics::getTotalDeployments).sum();
        int successfulDeployments = memberMetrics.stream().mapToInt(PeerComparisonDTO.PeerMetrics::getSuccessfulDeployments).sum();
        int failedDeployments = memberMetrics.stream().mapToInt(PeerComparisonDTO.PeerMetrics::getFailedDeployments).sum();
        int totalBuilds = memberMetrics.stream().mapToInt(PeerComparisonDTO.PeerMetrics::getTotalBuilds).sum();
        int successfulBuilds = memberMetrics.stream().mapToInt(PeerComparisonDTO.PeerMetrics::getSuccessfulBuilds).sum();
        int failedBuilds = memberMetrics.stream().mapToInt(PeerComparisonDTO.PeerMetrics::getFailedBuilds).sum();
        int deploymentsLast7Days = memberMetrics.stream().mapToInt(PeerComparisonDTO.PeerMetrics::getDeploymentsLast7Days).sum();
        int deploymentsLast30Days = memberMetrics.stream().mapToInt(PeerComparisonDTO.PeerMetrics::getDeploymentsLast30Days).sum();

        double successRate = totalDeployments > 0 ? (double) successfulDeployments / totalDeployments * 100 : 0;
        double buildSuccessRate = totalBuilds > 0 ? (double) successfulBuilds / totalBuilds * 100 : 0;
        double avgDeploymentTime = memberMetrics.stream()
                .mapToDouble(PeerComparisonDTO.PeerMetrics::getAvgDeploymentTimeMinutes)
                .average()
                .orElse(0);
        double avgBuildTime = memberMetrics.stream()
                .mapToDouble(PeerComparisonDTO.PeerMetrics::getAvgBuildTimeMinutes)
                .average()
                .orElse(0);

        return PeerComparisonDTO.PeerMetrics.builder()
                .totalDeployments(totalDeployments)
                .successfulDeployments(successfulDeployments)
                .failedDeployments(failedDeployments)
                .successRate(successRate)
                .totalBuilds(totalBuilds)
                .successfulBuilds(successfulBuilds)
                .failedBuilds(failedBuilds)
                .buildSuccessRate(buildSuccessRate)
                .avgDeploymentTimeMinutes(avgDeploymentTime)
                .avgBuildTimeMinutes(avgBuildTime)
                .deploymentsLast7Days(deploymentsLast7Days)
                .deploymentsLast30Days(deploymentsLast30Days)
                .build();
    }

    private void calculateRanksAndPercentiles(List<PeerComparisonDTO> comparisons) {
        if (comparisons.isEmpty()) {
            return;
        }

        // Sort by success rate (descending)
        comparisons.sort(Comparator.comparingDouble(c -> c.getMetrics().getSuccessRate()));
        Collections.reverse(comparisons);

        // Assign ranks
        for (int i = 0; i < comparisons.size(); i++) {
            comparisons.get(i).setRank(i + 1);
            double percentile = ((double) (comparisons.size() - i) / comparisons.size()) * 100;
            comparisons.get(i).setPercentile(percentile);
        }
    }

    private double calculateAverageDeploymentTime(List<DeploymentSet> deploymentSets) {
        if (deploymentSets.isEmpty()) {
            return 0;
        }

        double totalTime = 0;
        int count = 0;
        for (DeploymentSet ds : deploymentSets) {
            if (ds.getCreatedTime() != null && ds.getUpdatedTime() != null) {
                long durationMinutes = (ds.getUpdatedTime().getTime() - ds.getCreatedTime().getTime()) / (1000 * 60);
                totalTime += durationMinutes;
                count++;
            }
        }

        return count > 0 ? totalTime / count : 0;
    }

    private double calculateAverageBuildTime(List<ReleaseCandidate> builds) {
        if (builds.isEmpty()) {
            return 0;
        }

        double totalTime = 0;
        int count = 0;
        for (ReleaseCandidate rc : builds) {
            if (rc.getCreatedTime() != null && rc.getUpdatedTime() != null) {
                long durationMinutes = (rc.getUpdatedTime().getTime() - rc.getCreatedTime().getTime()) / (1000 * 60);
                totalTime += durationMinutes;
                count++;
            }
        }

        return count > 0 ? totalTime / count : 0;
    }

    private PeerComparisonDTO.DeploymentTimeComparison calculateTimeComparison(
            String peerId, String peerName, List<PeerComparisonDTO.DeploymentTimeEntry> timeEntries) {
        if (timeEntries.isEmpty()) {
            return PeerComparisonDTO.DeploymentTimeComparison.builder()
                    .peerId(peerId)
                    .peerName(peerName)
                    .deploymentTimes(Collections.emptyList())
                    .avgDeploymentTimeMinutes(0)
                    .medianDeploymentTimeMinutes(0)
                    .minDeploymentTimeMinutes(0)
                    .maxDeploymentTimeMinutes(0)
                    .build();
        }

        double avgTime = timeEntries.stream()
                .mapToDouble(PeerComparisonDTO.DeploymentTimeEntry::getDurationMinutes)
                .average()
                .orElse(0);

        List<Double> durations = timeEntries.stream()
                .map(PeerComparisonDTO.DeploymentTimeEntry::getDurationMinutes)
                .sorted()
                .collect(Collectors.toList());

        double medianTime = durations.get(durations.size() / 2);
        if (durations.size() % 2 == 0) {
            medianTime = (durations.get(durations.size() / 2 - 1) + durations.get(durations.size() / 2)) / 2;
        }

        double minTime = durations.get(0);
        double maxTime = durations.get(durations.size() - 1);

        return PeerComparisonDTO.DeploymentTimeComparison.builder()
                .peerId(peerId)
                .peerName(peerName)
                .deploymentTimes(timeEntries)
                .avgDeploymentTimeMinutes(avgTime)
                .medianDeploymentTimeMinutes(medianTime)
                .minDeploymentTimeMinutes(minTime)
                .maxDeploymentTimeMinutes(maxTime)
                .build();
    }
}
