package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudProviderResponse {
    private String id;
    private String name;
    private String location;
    private String description;
}
