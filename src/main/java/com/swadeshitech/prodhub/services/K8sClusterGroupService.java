package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.K8sClusterGroupRequest;
import com.swadeshitech.prodhub.dto.K8sClusterGroupResponse;
import com.swadeshitech.prodhub.entity.K8sClusterGroup;

import java.util.List;

public interface K8sClusterGroupService {
    
    K8sClusterGroupResponse createClusterGroup(K8sClusterGroupRequest request);
    
    K8sClusterGroupResponse getClusterGroup(String groupId);
    
    List<K8sClusterGroupResponse> getAllClusterGroups();
    
    K8sClusterGroupResponse updateClusterGroup(String groupId, K8sClusterGroupRequest request);
    
    void deleteClusterGroup(String groupId);
    
    void addClusterToGroup(String groupId, String clusterId);
    
    void removeClusterFromGroup(String groupId, String clusterId);
}
