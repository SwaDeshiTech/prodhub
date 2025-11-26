package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.DeploymentRunStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deployment_runs")
@EqualsAndHashCode(callSuper = true)
@Builder
public class DeploymentRun extends BaseEntity {

    @Id
    private String id;

    private DeploymentRunStatus deploymentRunStatus;

    private DeploymentTemplate deploymentTemplate;

    @DBRef
    private transient Metadata metaData;

    @DBRef
    private transient DeploymentSet deploymentSet;
}
