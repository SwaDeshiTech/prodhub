package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.enums.CloudProviderState;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudProviderRegisterResponse {
    private String id;
    private String name;
    private boolean isActive;
    private CloudProviderState state;
}
