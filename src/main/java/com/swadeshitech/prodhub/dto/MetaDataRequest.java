package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.enums.ProfileType;

import lombok.Data;

@Data
public class MetaDataRequest {
    private String id;
    private boolean isActive;
    private String name;
    private String data;
    private ProfileType profileType;
    private String description;
    private String referencedProfileId;
}
