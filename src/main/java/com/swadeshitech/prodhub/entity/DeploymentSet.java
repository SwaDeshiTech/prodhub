package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.DeploymentSetStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private Map<String, String> metaData;

    private DeploymentSetStatus status;

    @DBRef
    private Application application;

    @DBRef
    private Metadata deploymentProfile;

    @DBRef
    private Approvals approvals;

    @DBRef
    private ReleaseCandidate releaseCandidate;
}
