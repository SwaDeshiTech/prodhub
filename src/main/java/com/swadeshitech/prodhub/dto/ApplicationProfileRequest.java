package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Data;

@Data
public class ApplicationProfileRequest {
    private String applicationId;
    private MetaDataRequest profile;
}
