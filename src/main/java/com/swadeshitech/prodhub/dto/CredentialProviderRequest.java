package com.swadeshitech.prodhub.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialProviderRequest {
    private String name;
    private String description;
    private String provider;
    private String metaData;
    private String serviceId;
}
