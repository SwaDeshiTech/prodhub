package com.swadeshitech.prodhub.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "k8sClusterGroups")
@EqualsAndHashCode(callSuper = true)
@Builder
public class K8sClusterGroup extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private boolean isActive;

    @DBRef
    private Set<CredentialProvider> clusters;

    private String schedulingStrategy; // RESOURCE_BASED, ROUND_ROBIN, TEAM_BASED, DEPARTMENT_BASED

    private Integer priority; // Higher priority groups are selected first
}
