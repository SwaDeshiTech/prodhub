package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
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
@Document(collection = "release_candidates")
@EqualsAndHashCode(callSuper = true)
@Builder
public class ReleaseCandidate extends BaseEntity implements Serializable {

    @Id
    private String id;

    private Map<String, String> metaData;

    private ReleaseCandidateStatus status;

    private String buildProfile;

    private Map<String, String> metadata;

    private String ephemeralEnvironmentName;

    private String buildRefId;

    @DBRef
    private User initiatedBy;

    @DBRef
    private User certifiedBy;

    @DBRef
    private Application service;
}