package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class SCMResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private boolean isActive;
    private String location;
}
