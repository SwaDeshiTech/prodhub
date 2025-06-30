package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class ResourceDetailsRegisterRequest {
    private String name;
    private String resourceType;
    private String meta;
    private String cloudProvider;
}
