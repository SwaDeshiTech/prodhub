package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthProviderResponse {
    private String id;
    private String name;
    private String providerType;
    private String displayName;
    private String description;
    private Boolean isActive;
    private Boolean isDefault;
    private String redirectUrl;
    private List<String> scopes;
    private String authUrl;
    private String tokenUrl;
    private String userInfoUrl;
    private String logoUrl;
    private Integer sortOrder;
    private String createdTime;
    private String lastModifiedTime;
}
