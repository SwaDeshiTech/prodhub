package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApprovalRequest {
    private String serviceId;
    private String comment;
    private MetaDataRequest metaDataRequest;
}
