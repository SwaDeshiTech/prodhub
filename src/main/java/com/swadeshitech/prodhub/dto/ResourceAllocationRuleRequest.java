package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAllocationRuleRequest {
    private String name;
    private String description;
    private String clusterGroupId;
    private String organizationId;
    private String teamLeadId;
    private String ruleType; // TEAM_BASED, DEPARTMENT_BASED, RESOURCE_BASED, ROUND_ROBIN
    private Map<String, String> criteria;
    private Integer priority;
    private Boolean isActive;
}
