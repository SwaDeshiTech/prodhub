package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class ApprovalRequest {
    private String serviceId;
    private String comment;
    private MetaDataRequest metaDataRequest;
}
