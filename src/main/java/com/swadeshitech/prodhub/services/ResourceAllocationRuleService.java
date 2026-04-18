package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.ResourceAllocationRuleRequest;
import com.swadeshitech.prodhub.dto.ResourceAllocationRuleResponse;

import java.util.List;

public interface ResourceAllocationRuleService {
    
    ResourceAllocationRuleResponse createRule(ResourceAllocationRuleRequest request);
    
    ResourceAllocationRuleResponse getRule(String ruleId);
    
    List<ResourceAllocationRuleResponse> getAllRules();
    
    ResourceAllocationRuleResponse updateRule(String ruleId, ResourceAllocationRuleRequest request);
    
    void deleteRule(String ruleId);
}
