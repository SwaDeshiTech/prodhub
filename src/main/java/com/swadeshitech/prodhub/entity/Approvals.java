package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.ApprovalStatus;
import com.swadeshitech.prodhub.enums.ProfileType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "approvals")
@EqualsAndHashCode(callSuper = true)
@Builder
public class Approvals extends BaseEntity {

    @Id
    private String id;

    private String comment;

    private String updatedMetaData;

    private ApprovalStatus approvalStatus;

    private String profileName;

    private ProfileType profileType;

    private Map<String, Object> metaData;

    @DBRef
    private transient ApprovalStage approvalStage;

    @DBRef
    private transient Metadata currentMetadata;

    @DBRef
    private transient Application application;
}
