package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class CredentialProviderFilter {
    private String name;
    private String type;
    private boolean isActive;
    private String applicationId;
}
