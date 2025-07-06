package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudProviderDetailsResponse {
    private String id;
    private String name;
    private String description;
    private String metaData;
}
