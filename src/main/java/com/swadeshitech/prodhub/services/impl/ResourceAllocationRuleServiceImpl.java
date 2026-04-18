package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.ResourceAllocationRuleRequest;
import com.swadeshitech.prodhub.dto.ResourceAllocationRuleResponse;
import com.swadeshitech.prodhub.entity.K8sClusterGroup;
import com.swadeshitech.prodhub.entity.Organization;
import com.swadeshitech.prodhub.entity.ResourceAllocationRule;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.K8sClusterGroupRepository;
import com.swadeshitech.prodhub.repository.ResourceAllocationRuleRepository;
import com.swadeshitech.prodhub.services.ResourceAllocationRuleService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResourceAllocationRuleServiceImpl implements ResourceAllocationRuleService {

    @Autowired
    private ResourceAllocationRuleRepository resourceAllocationRuleRepository;

    @Autowired
    private K8sClusterGroupRepository k8sClusterGroupRepository;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Override
    public ResourceAllocationRuleResponse createRule(ResourceAllocationRuleRequest request) {
        log.info("Creating resource allocation rule: {}", request.getName());

        // Validate cluster group
        if (request.getClusterGroupId() != null) {
            Optional<K8sClusterGroup> groupOpt = k8sClusterGroupRepository.findById(request.getClusterGroupId());
            if (groupOpt.isEmpty()) {
                log.error("Cluster group not found with id: {}", request.getClusterGroupId());
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        // Validate organization if provided
        Organization organization = null;
        if (request.getOrganizationId() != null) {
            List<Organization> orgs = readTransactionService.findByDynamicOrFilters(
                    Map.of("_id", new org.bson.types.ObjectId(request.getOrganizationId())),
                    Organization.class);
            if (orgs.isEmpty()) {
                log.error("Organization not found with id: {}", request.getOrganizationId());
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            organization = orgs.get(0);
        }

        // Validate team lead if provided
        User teamLead = null;
        if (request.getTeamLeadId() != null) {
            List<User> users = readTransactionService.findByDynamicOrFilters(
                    Map.of("_id", new org.bson.types.ObjectId(request.getTeamLeadId())),
                    User.class);
            if (users.isEmpty()) {
                log.error("User not found with id: {}", request.getTeamLeadId());
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            teamLead = users.get(0);
        }

        ResourceAllocationRule rule = ResourceAllocationRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .clusterGroup(request.getClusterGroupId() != null 
                        ? k8sClusterGroupRepository.findById(request.getClusterGroupId()).orElse(null) 
                        : null)
                .organization(organization)
                .teamLead(teamLead)
                .ruleType(request.getRuleType() != null ? request.getRuleType() : "RESOURCE_BASED")
                .criteria(request.getCriteria())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(true)
                .build();

        rule = resourceAllocationRuleRepository.save(rule);
        log.info("Resource allocation rule created with id: {}", rule.getId());

        return mapToResponse(rule);
    }

    @Override
    public ResourceAllocationRuleResponse getRule(String ruleId) {
        Optional<ResourceAllocationRule> ruleOpt = resourceAllocationRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) {
            log.error("Resource allocation rule not found with id: {}", ruleId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return mapToResponse(ruleOpt.get());
    }

    @Override
    public List<ResourceAllocationRuleResponse> getAllRules() {
        List<ResourceAllocationRule> rules = resourceAllocationRuleRepository.findAll();
        return rules.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ResourceAllocationRuleResponse updateRule(String ruleId, ResourceAllocationRuleRequest request) {
        Optional<ResourceAllocationRule> ruleOpt = resourceAllocationRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) {
            log.error("Resource allocation rule not found with id: {}", ruleId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        ResourceAllocationRule rule = ruleOpt.get();

        if (request.getName() != null) {
            rule.setName(request.getName());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        if (request.getRuleType() != null) {
            rule.setRuleType(request.getRuleType());
        }
        if (request.getCriteria() != null) {
            rule.setCriteria(request.getCriteria());
        }
        if (request.getPriority() != null) {
            rule.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            rule.setActive(request.getIsActive());
        }

        if (request.getClusterGroupId() != null) {
            Optional<K8sClusterGroup> groupOpt = k8sClusterGroupRepository.findById(request.getClusterGroupId());
            if (groupOpt.isPresent()) {
                rule.setClusterGroup(groupOpt.get());
            }
        }

        rule = resourceAllocationRuleRepository.save(rule);
        log.info("Resource allocation rule updated: {}", ruleId);

        return mapToResponse(rule);
    }

    @Override
    public void deleteRule(String ruleId) {
        if (!resourceAllocationRuleRepository.existsById(ruleId)) {
            log.error("Resource allocation rule not found with id: {}", ruleId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        resourceAllocationRuleRepository.deleteById(ruleId);
        log.info("Resource allocation rule deleted: {}", ruleId);
    }

    private ResourceAllocationRuleResponse mapToResponse(ResourceAllocationRule rule) {
        ResourceAllocationRuleResponse response = new ResourceAllocationRuleResponse();
        response.setId(rule.getId());
        response.setName(rule.getName());
        response.setDescription(rule.getDescription());
        response.setClusterGroupId(rule.getClusterGroup() != null ? rule.getClusterGroup().getId() : null);
        response.setClusterGroupName(rule.getClusterGroup() != null ? rule.getClusterGroup().getName() : null);
        response.setOrganizationId(rule.getOrganization() != null ? rule.getOrganization().getId() : null);
        response.setOrganizationName(rule.getOrganization() != null ? rule.getOrganization().getName() : null);
        response.setTeamLeadId(rule.getTeamLead() != null ? rule.getTeamLead().getId() : null);
        response.setTeamLeadName(rule.getTeamLead() != null ? rule.getTeamLead().getName() : null);
        response.setRuleType(rule.getRuleType());
        response.setCriteria(rule.getCriteria());
        response.setPriority(rule.getPriority());
        response.setActive(rule.isActive());
        response.setCreatedTime(rule.getCreatedTime());
        response.setLastModifiedTime(rule.getLastModifiedTime());
        return response;
    }
}
