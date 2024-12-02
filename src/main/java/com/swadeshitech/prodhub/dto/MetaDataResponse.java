package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.enums.ProfileType;

import lombok.Data;

@Data
public class MetaDataResponse {
    private String name;
    private String data;
    private ProfileType profileType;
    private boolean isActive;
}
