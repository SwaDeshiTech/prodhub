package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.K8sClusterGroupRequest;
import com.swadeshitech.prodhub.dto.K8sClusterGroupResponse;
import com.swadeshitech.prodhub.entity.CredentialProvider;
import com.swadeshitech.prodhub.entity.K8sClusterGroup;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.K8sClusterGroupRepository;
import com.swadeshitech.prodhub.services.K8sClusterGroupService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class K8sClusterGroupServiceImpl implements K8sClusterGroupService {

    @Autowired
    private K8sClusterGroupRepository clusterGroupRepository;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Override
    public K8sClusterGroupResponse createClusterGroup(K8sClusterGroupRequest request) {
        log.info("Creating k8s cluster group: {}", request.getName());

        // Validate cluster IDs
        Set<CredentialProvider> clusters = new HashSet<>();
        if (request.getClusterIds() != null && !request.getClusterIds().isEmpty()) {
            for (String clusterId : request.getClusterIds()) {
                List<CredentialProvider> clusterList = readTransactionService.findCredentialProviderByFilters(
                        Map.of("_id", new org.bson.types.ObjectId(clusterId)));
                if (clusterList.isEmpty()) {
                    log.error("Cluster not found with id: {}", clusterId);
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
                clusters.add(clusterList.get(0));
            }
        }

        K8sClusterGroup clusterGroup = K8sClusterGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .clusters(clusters)
                .schedulingStrategy(request.getSchedulingStrategy() != null ? request.getSchedulingStrategy() : "RESOURCE_BASED")
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(true)
                .build();

        clusterGroup = clusterGroupRepository.save(clusterGroup);
        log.info("K8s cluster group created with id: {}", clusterGroup.getId());

        return mapToResponse(clusterGroup);
    }

    @Override
    public K8sClusterGroupResponse getClusterGroup(String groupId) {
        Optional<K8sClusterGroup> groupOpt = clusterGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            log.error("Cluster group not found with id: {}", groupId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return mapToResponse(groupOpt.get());
    }

    @Override
    public List<K8sClusterGroupResponse> getAllClusterGroups() {
        List<K8sClusterGroup> groups = clusterGroupRepository.findAll();
        return groups.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public K8sClusterGroupResponse updateClusterGroup(String groupId, K8sClusterGroupRequest request) {
        Optional<K8sClusterGroup> groupOpt = clusterGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            log.error("Cluster group not found with id: {}", groupId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        K8sClusterGroup group = groupOpt.get();

        if (request.getName() != null) {
            group.setName(request.getName());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getSchedulingStrategy() != null) {
            group.setSchedulingStrategy(request.getSchedulingStrategy());
        }
        if (request.getPriority() != null) {
            group.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            group.setActive(request.getIsActive());
        }

        if (request.getClusterIds() != null) {
            Set<CredentialProvider> clusters = new HashSet<>();
            for (String clusterId : request.getClusterIds()) {
                List<CredentialProvider> clusterList = readTransactionService.findCredentialProviderByFilters(
                        Map.of("_id", new org.bson.types.ObjectId(clusterId)));
                if (!clusterList.isEmpty()) {
                    clusters.add(clusterList.get(0));
                }
            }
            group.setClusters(clusters);
        }

        group = clusterGroupRepository.save(group);
        log.info("K8s cluster group updated: {}", groupId);

        return mapToResponse(group);
    }

    @Override
    public void deleteClusterGroup(String groupId) {
        if (!clusterGroupRepository.existsById(groupId)) {
            log.error("Cluster group not found with id: {}", groupId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        clusterGroupRepository.deleteById(groupId);
        log.info("K8s cluster group deleted: {}", groupId);
    }

    @Override
    public void addClusterToGroup(String groupId, String clusterId) {
        Optional<K8sClusterGroup> groupOpt = clusterGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            log.error("Cluster group not found with id: {}", groupId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        List<CredentialProvider> clusterList = readTransactionService.findCredentialProviderByFilters(
                Map.of("_id", new org.bson.types.ObjectId(clusterId)));
        if (clusterList.isEmpty()) {
            log.error("Cluster not found with id: {}", clusterId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        K8sClusterGroup group = groupOpt.get();
        if (group.getClusters() == null) {
            group.setClusters(new HashSet<>());
        }
        group.getClusters().add(clusterList.get(0));
        clusterGroupRepository.save(group);

        log.info("Cluster {} added to group {}", clusterId, groupId);
    }

    @Override
    public void removeClusterFromGroup(String groupId, String clusterId) {
        Optional<K8sClusterGroup> groupOpt = clusterGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            log.error("Cluster group not found with id: {}", groupId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        List<CredentialProvider> clusterList = readTransactionService.findCredentialProviderByFilters(
                Map.of("_id", new org.bson.types.ObjectId(clusterId)));
        if (clusterList.isEmpty()) {
            log.error("Cluster not found with id: {}", clusterId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        K8sClusterGroup group = groupOpt.get();
        if (group.getClusters() != null) {
            group.getClusters().remove(clusterList.get(0));
            clusterGroupRepository.save(group);
        }

        log.info("Cluster {} removed from group {}", clusterId, groupId);
    }

    private K8sClusterGroupResponse mapToResponse(K8sClusterGroup group) {
        List<String> clusterIds = group.getClusters() != null
                ? group.getClusters().stream().map(CredentialProvider::getId).collect(Collectors.toList())
                : new ArrayList<>();

        K8sClusterGroupResponse response = new K8sClusterGroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setClusterIds(clusterIds);
        response.setSchedulingStrategy(group.getSchedulingStrategy());
        response.setPriority(group.getPriority());
        response.setActive(group.isActive());
        response.setCreatedTime(group.getCreatedTime());
        response.setLastModifiedTime(group.getLastModifiedTime());
        return response;
    }
}
