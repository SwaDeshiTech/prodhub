package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class K8sClusterGroupRequest {
    private String name;
    private String description;
    private List<String> clusterIds;
    private String schedulingStrategy; // RESOURCE_BASED, ROUND_ROBIN
    private Integer priority;
    private Boolean isActive;
}
