package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class ApplicationProfileRequest {
    private String applicationId;
    private MetaDataRequest profile;
}
