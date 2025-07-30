package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class SCMDetailsResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private String metaData;
    private boolean isActive;
}