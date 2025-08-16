package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class BuildProviderResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private String location;
    private boolean isActive;
    private String metaData;
    private String buildProviderType;
}
