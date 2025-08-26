package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.enums.ProfileType;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class MetaDataResponse extends BaseResponse {
    private String name;
    private String data;
    private ProfileType profileType;
    private boolean isActive;
    private String referencedProfileId;
    private String description;
}
