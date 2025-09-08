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
@Document(collection = "credentialProvider")
@EqualsAndHashCode(callSuper = true)
@Builder
public class CredentialProvider extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private boolean isActive;

    private String credentialPath;

    private com.swadeshitech.prodhub.enums.CredentialProvider credentialProvider;

    @DBRef
    private Application application;
}
