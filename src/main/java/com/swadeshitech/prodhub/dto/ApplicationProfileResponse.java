package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class ApplicationProfileResponse {
    private String applicationId;
    private MetaDataResponse profile;
}
