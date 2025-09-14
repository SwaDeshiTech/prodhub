package com.swadeshitech.prodhub.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialProviderResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private String credentialPath;
    private boolean isActive;
    private String type;
    private String serviceName;
    private String serviceId;
    private String credentialMetadata;
}