package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceDetailsResponse {
    private String id;
    private String name;
    private String resourceType;
    private String meta;
    private String cloudProvider;
}
