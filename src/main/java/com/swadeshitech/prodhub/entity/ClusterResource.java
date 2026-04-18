package com.swadeshitech.prodhub.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clusterResources")
@EqualsAndHashCode(callSuper = true)
@Builder
public class ClusterResource extends BaseEntity implements Serializable {

    @Id
    private String id;

    @DBRef
    @Indexed
    private CredentialProvider cluster;

    private Integer cpuCapacity; // Total CPU capacity in cores
    private Integer memoryCapacity; // Total memory capacity in GB
    private Integer cpuAvailable; // Available CPU in cores
    private Integer memoryAvailable; // Available memory in GB

    private Integer maxEphemeralEnvironments; // Max number of ephemeral environments allowed
    private Integer currentEphemeralEnvironments; // Current number of ephemeral environments
}
