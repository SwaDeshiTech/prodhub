package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResourceAllocationRuleResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private String clusterGroupId;
    private String clusterGroupName;
    private String organizationId;
    private String organizationName;
    private String teamLeadId;
    private String teamLeadName;
    private String ruleType;
    private Map<String, String> criteria;
    private Integer priority;
    private boolean isActive;
}
