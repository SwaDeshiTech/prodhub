package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationProfileRequest {
    private String applicationId;
    private MetaDataRequest profile;
    private String initiatedBy;
}
