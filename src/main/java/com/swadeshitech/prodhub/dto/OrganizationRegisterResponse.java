package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class OrganizationRegisterResponse extends BaseResponse {
    private String id;
    private String name;
    private String type;
    private String description;
    private boolean active;
}
