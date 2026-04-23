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
@Document(collection = "service_approval_configs")
@EqualsAndHashCode(callSuper = true)
@Builder
public class ServiceApprovalConfig extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String serviceId; // Reference to Application ID

    private String serviceName; // Denormalized for easy access

    @DBRef
    private ApprovalFlow approvalFlow; // The specific approval flow for this service

    private boolean useCustomFlow; // If true, use the custom flow; if false, use default/fallback

    private boolean overrideDefault; // If true, this config overrides the default flow
}
