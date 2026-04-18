package com.swadeshitech.prodhub.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "resourceAllocationRules")
@EqualsAndHashCode(callSuper = true)
@Builder
public class ResourceAllocationRule extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private boolean isActive;

    @DBRef
    @Indexed
    private K8sClusterGroup clusterGroup;

    @DBRef
    @Indexed
    private Organization organization; // Optional: For organization-level rules

    @DBRef
    @Indexed
    private User teamLead; // Optional: For team-level rules

    private String ruleType; // TEAM_BASED, DEPARTMENT_BASED, RESOURCE_BASED, ROUND_ROBIN

    private Map<String, String> criteria; // Flexible criteria: e.g., {"department": "engineering", "team": "frontend"}

    private Integer priority; // Higher priority rules are evaluated first
}
