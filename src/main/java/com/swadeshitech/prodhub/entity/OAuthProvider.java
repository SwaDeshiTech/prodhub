package com.swadeshitech.prodhub.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "oauth_providers")
@EqualsAndHashCode(callSuper = true)
@Builder
public class OAuthProvider extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    @Indexed
    private String providerType; // GOOGLE, GITHUB, etc.

    private String displayName;

    private String description;

    private boolean isActive;

    private boolean isDefault;

    private String clientId;

    private String clientSecret;

    private String redirectUrl;

    private List<String> scopes;

    private String authUrl;

    private String tokenUrl;

    private String userInfoUrl;

    private String logoUrl;

    private Integer sortOrder;
}
