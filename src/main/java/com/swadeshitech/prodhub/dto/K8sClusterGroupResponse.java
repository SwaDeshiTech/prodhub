package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class K8sClusterGroupResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private List<String> clusterIds;
    private String schedulingStrategy;
    private Integer priority;
    private boolean isActive;
}
