package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.entity.CredentialProvider;

import java.util.Map;

public interface ResourceSchedulerService {
    
    /**
     * Allocate a k8s cluster for ephemeral environment based on scheduling rules
     * 
     * @param organizationId Organization ID (optional)
     * @param userId User ID (optional, for team-based scheduling)
     * @param requiredCpu Required CPU in cores
     * @param requiredMemory Required memory in GB
     * @return Allocated CredentialProvider (k8s cluster)
     */
    CredentialProvider allocateCluster(String organizationId, String userId, Integer requiredCpu, Integer requiredMemory);
    
    /**
     * Release resources when ephemeral environment is deleted
     * 
     * @param clusterId The cluster ID
     * @param cpuReleased CPU to release
     * @param memoryReleased Memory to release
     */
    void releaseClusterResources(String clusterId, Integer cpuReleased, Integer memoryReleased);
    
    /**
     * Update ephemeral environment with allocated cluster
     * 
     * @param ephemeralEnvironmentId Ephemeral environment ID
     * @param clusterId Allocated cluster ID
     */
    void updateEphemeralEnvironmentClusterAllocation(String ephemeralEnvironmentId, String clusterId);
}
