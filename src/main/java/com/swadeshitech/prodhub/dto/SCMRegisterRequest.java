package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class SCMRegisterRequest {
    private String name;
    private String metaData;
    private String description;
}
