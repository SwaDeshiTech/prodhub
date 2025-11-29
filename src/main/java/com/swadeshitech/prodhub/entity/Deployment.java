package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.DeploymentStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deployment")
@EqualsAndHashCode(callSuper = true)
@Builder
public class Deployment extends BaseEntity {

    @Id
    private String id;

    private DeploymentStatus status;

    private Map<String, Object> metaData;

    private DeploymentTemplate deploymentTemplate;

    @DBRef
    private transient DeploymentSet deploymentSet;

    @DBRef
    private transient Application application;
}

