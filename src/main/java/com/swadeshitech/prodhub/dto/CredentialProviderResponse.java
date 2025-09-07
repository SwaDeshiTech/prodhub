package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class CredentialProviderResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private String credentialPath;
    private boolean isActive;
    private String type;
    private String serviceName;
}