package com.swadeshitech.prodhub.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_organizations")
@CompoundIndex(name = "user_organization_idx", def = "{'userId': 1, 'organizationId': 1}", unique = true)
public class UserOrganization extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String organizationId;

    @DBRef
    private User user;

    @DBRef
    private Organization organization;

    private String role; // e.g., "ADMIN", "MEMBER", "OWNER"

    private boolean isActive;
}
