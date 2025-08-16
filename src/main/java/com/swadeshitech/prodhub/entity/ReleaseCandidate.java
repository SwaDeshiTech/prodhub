package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.swadeshitech.prodhub.enums.ReleaseCandidateStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "buildProviders")
@EqualsAndHashCode(callSuper = true)
@Builder
public class ReleaseCandidate extends BaseEntity implements Serializable {

    @Id
    private String id;

    private Map<String, String> metaData;

    private ReleaseCandidateStatus status;

    private User initiatedBy;

    private User certifiedBy;

    private String serviceName;

    private String buildProfile;
}