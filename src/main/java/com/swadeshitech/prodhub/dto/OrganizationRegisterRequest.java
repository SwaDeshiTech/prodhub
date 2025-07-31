package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class OrganizationRegisterRequest {
    private String name;
    private String type;
    private String description;
}