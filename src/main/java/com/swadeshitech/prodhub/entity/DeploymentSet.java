package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.DeploymentSetStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deployment_set")
@EqualsAndHashCode(callSuper = true)
@Builder
public class DeploymentSet extends BaseEntity {

    @Id
    private String id;

    private String uuid;

    private Map<String, Object> metaData;

    private DeploymentSetStatus status;

    @DBRef
    private transient Application application;

    @DBRef
    private transient Metadata deploymentProfile;

    @DBRef
    private transient Approvals approvals;

    @DBRef
    private transient ReleaseCandidate releaseCandidate;

    @DBRef
    private transient List<DeploymentRun> deploymentRuns;
}
