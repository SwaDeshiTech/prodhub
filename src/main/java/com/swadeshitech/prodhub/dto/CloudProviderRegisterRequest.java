package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class CloudProviderRegisterRequest {
    private String name;
    private String metaData;
    private String description;
}
