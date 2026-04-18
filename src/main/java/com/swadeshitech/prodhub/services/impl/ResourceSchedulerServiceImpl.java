package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ClusterResourceRepository;
import com.swadeshitech.prodhub.repository.K8sClusterGroupRepository;
import com.swadeshitech.prodhub.repository.ResourceAllocationRuleRepository;
import com.swadeshitech.prodhub.services.ResourceSchedulerService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResourceSchedulerServiceImpl implements ResourceSchedulerService {

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Autowired
    private ResourceAllocationRuleRepository resourceAllocationRuleRepository;

    @Autowired
    private ClusterResourceRepository clusterResourceRepository;

    @Override
    public CredentialProvider allocateCluster(String organizationId, String userId, Integer requiredCpu, Integer requiredMemory) {
        log.info("Allocating cluster for ephemeral environment - org: {}, user: {}, cpu: {}, memory: {}", 
                organizationId, userId, requiredCpu, requiredMemory);

        // Get active allocation rules ordered by priority
        List<ResourceAllocationRule> activeRules = resourceAllocationRuleRepository.findByIsActiveTrueOrderByPriorityDesc();
        
        // Filter rules based on organization or user if provided
        List<ResourceAllocationRule> applicableRules = filterApplicableRules(activeRules, organizationId, userId);
        
        if (applicableRules.isEmpty()) {
            log.warn("No applicable allocation rules found, using default resource-based allocation");
            return allocateBasedOnResources(requiredCpu, requiredMemory);
        }

        // Try each rule in priority order
        for (ResourceAllocationRule rule : applicableRules) {
            try {
                CredentialProvider cluster = allocateBasedOnRule(rule, requiredCpu, requiredMemory);
                if (cluster != null) {
                    log.info("Successfully allocated cluster {} using rule {}", cluster.getId(), rule.getName());
                    return cluster;
                }
            } catch (Exception e) {
                log.warn("Failed to allocate using rule {}, trying next rule", rule.getName(), e);
            }
        }

        // Fallback to resource-based allocation if no rule succeeded
        log.warn("All allocation rules failed, falling back to resource-based allocation");
        return allocateBasedOnResources(requiredCpu, requiredMemory);
    }

    private List<ResourceAllocationRule> filterApplicableRules(List<ResourceAllocationRule> rules, String organizationId, String userId) {
        return rules.stream()
                .filter(rule -> {
                    // If rule has organization filter, check if it matches
                    if (rule.getOrganization() != null && organizationId != null) {
                        return rule.getOrganization().getId().equals(organizationId);
                    }
                    // If rule has team lead filter, check if it matches
                    if (rule.getTeamLead() != null && userId != null) {
                        return rule.getTeamLead().getId().equals(userId);
                    }
                    // If no specific filters, rule is applicable
                    return rule.getOrganization() == null && rule.getTeamLead() == null;
                })
                .collect(Collectors.toList());
    }

    private CredentialProvider allocateBasedOnRule(ResourceAllocationRule rule, Integer requiredCpu, Integer requiredMemory) {
        K8sClusterGroup clusterGroup = rule.getClusterGroup();
        if (clusterGroup == null || clusterGroup.getClusters() == null || clusterGroup.getClusters().isEmpty()) {
            log.warn("No clusters in group {}", rule.getName());
            return null;
        }

        String strategy = clusterGroup.getSchedulingStrategy();
        if (strategy == null) {
            strategy = "RESOURCE_BASED";
        }

        switch (strategy) {
            case "ROUND_ROBIN":
                return allocateRoundRobin(clusterGroup, requiredCpu, requiredMemory);
            case "RESOURCE_BASED":
                return allocateBasedOnResources(clusterGroup, requiredCpu, requiredMemory);
            default:
                log.warn("Unknown scheduling strategy {}, defaulting to RESOURCE_BASED", strategy);
                return allocateBasedOnResources(clusterGroup, requiredCpu, requiredMemory);
        }
    }

    private CredentialProvider allocateRoundRobin(K8sClusterGroup clusterGroup, Integer requiredCpu, Integer requiredMemory) {
        // Simple round-robin implementation - can be enhanced with persistent state
        List<CredentialProvider> clusters = new ArrayList<>(clusterGroup.getClusters());
        
        for (CredentialProvider cluster : clusters) {
            ClusterResource resource = clusterResourceRepository.findByCluster(cluster).stream().findFirst().orElse(null);
            if (resource != null && hasSufficientResources(resource, requiredCpu, requiredMemory)) {
                // Allocate resources
                allocateResources(resource, requiredCpu, requiredMemory);
                clusterResourceRepository.save(resource);
                return cluster;
            }
        }
        
        log.warn("No cluster in group {} has sufficient resources", clusterGroup.getName());
        return null;
    }

    private CredentialProvider allocateBasedOnResources(Integer requiredCpu, Integer requiredMemory) {
        // Get all active cluster resources
        List<ClusterResource> allResources = clusterResourceRepository.findAll();
        
        // Filter clusters that have sufficient resources
        List<ClusterResource> availableClusters = allResources.stream()
                .filter(resource -> hasSufficientResources(resource, requiredCpu, requiredMemory))
                .sorted(Comparator.comparingInt((ClusterResource r) -> r.getCpuAvailable() + r.getMemoryAvailable()).reversed())
                .collect(Collectors.toList());

        if (availableClusters.isEmpty()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // Allocate to the cluster with most available resources
        ClusterResource selectedResource = availableClusters.get(0);
        allocateResources(selectedResource, requiredCpu, requiredMemory);
        clusterResourceRepository.save(selectedResource);

        return selectedResource.getCluster();
    }

    private CredentialProvider allocateBasedOnResources(K8sClusterGroup clusterGroup, Integer requiredCpu, Integer requiredMemory) {
        List<CredentialProvider> clusters = new ArrayList<>(clusterGroup.getClusters());
        
        // Get resources for each cluster in the group
        List<ClusterResource> groupResources = new ArrayList<>();
        for (CredentialProvider cluster : clusters) {
            List<ClusterResource> resources = clusterResourceRepository.findByCluster(cluster);
            if (!resources.isEmpty()) {
                groupResources.add(resources.get(0));
            }
        }

        // Filter clusters with sufficient resources
        List<ClusterResource> availableClusters = groupResources.stream()
                .filter(resource -> hasSufficientResources(resource, requiredCpu, requiredMemory))
                .sorted(Comparator.comparingInt((ClusterResource r) -> r.getCpuAvailable() + r.getMemoryAvailable()).reversed())
                .collect(Collectors.toList());

        if (availableClusters.isEmpty()) {
            log.warn("No cluster in group {} has sufficient resources", clusterGroup.getName());
            return null;
        }

        // Allocate to the cluster with most available resources
        ClusterResource selectedResource = availableClusters.get(0);
        allocateResources(selectedResource, requiredCpu, requiredMemory);
        clusterResourceRepository.save(selectedResource);

        return selectedResource.getCluster();
    }

    private boolean hasSufficientResources(ClusterResource resource, Integer requiredCpu, Integer requiredMemory) {
        boolean cpuSufficient = resource.getCpuAvailable() >= (requiredCpu != null ? requiredCpu : 0);
        boolean memorySufficient = resource.getMemoryAvailable() >= (requiredMemory != null ? requiredMemory : 0);
        boolean capacityNotReached = resource.getCurrentEphemeralEnvironments() < resource.getMaxEphemeralEnvironments();
        
        return cpuSufficient && memorySufficient && capacityNotReached;
    }

    private void allocateResources(ClusterResource resource, Integer cpu, Integer memory) {
        if (cpu != null) {
            resource.setCpuAvailable(resource.getCpuAvailable() - cpu);
        }
        if (memory != null) {
            resource.setMemoryAvailable(resource.getMemoryAvailable() - memory);
        }
        resource.setCurrentEphemeralEnvironments(resource.getCurrentEphemeralEnvironments() + 1);
    }

    @Override
    public void releaseClusterResources(String clusterId, Integer cpuReleased, Integer memoryReleased) {
        log.info("Releasing resources for cluster {} - cpu: {}, memory: {}", clusterId, cpuReleased, memoryReleased);

        List<CredentialProvider> clusters = readTransactionService.findCredentialProviderByFilters(
                Map.of("_id", new org.bson.types.ObjectId(clusterId)));
        
        if (clusters.isEmpty()) {
            log.error("Cluster not found with id {}", clusterId);
            return;
        }

        CredentialProvider cluster = clusters.getFirst();
        List<ClusterResource> resources = clusterResourceRepository.findByCluster(cluster);
        
        if (resources.isEmpty()) {
            log.error("No resource record found for cluster {}", clusterId);
            return;
        }

        ClusterResource resource = resources.get(0);
        
        if (cpuReleased != null) {
            resource.setCpuAvailable(resource.getCpuAvailable() + cpuReleased);
        }
        if (memoryReleased != null) {
            resource.setMemoryAvailable(resource.getMemoryAvailable() + memoryReleased);
        }
        resource.setCurrentEphemeralEnvironments(Math.max(0, resource.getCurrentEphemeralEnvironments() - 1));
        
        clusterResourceRepository.save(resource);
        log.info("Resources released successfully for cluster {}", clusterId);
    }

    @Override
    public void updateEphemeralEnvironmentClusterAllocation(String ephemeralEnvironmentId, String clusterId) {
        log.info("Updating ephemeral environment {} with cluster allocation {}", ephemeralEnvironmentId, clusterId);

        List<EphemeralEnvironment> environments = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", new org.bson.types.ObjectId(ephemeralEnvironmentId)),
                EphemeralEnvironment.class);

        if (environments.isEmpty()) {
            log.error("Ephemeral environment not found with id {}", ephemeralEnvironmentId);
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_NOT_FOUND);
        }

        EphemeralEnvironment environment = environments.getFirst();
        
        List<CredentialProvider> clusters = readTransactionService.findCredentialProviderByFilters(
                Map.of("_id", new org.bson.types.ObjectId(clusterId)));
        
        if (clusters.isEmpty()) {
            log.error("Cluster not found with id {}", clusterId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        environment.setK8sClusterAllocation(clusters.getFirst());
        writeTransactionService.saveEphemeralEnvironmentToRepository(environment);
        
        log.info("Ephemeral environment {} updated with cluster allocation {}", ephemeralEnvironmentId, clusterId);
    }
}
