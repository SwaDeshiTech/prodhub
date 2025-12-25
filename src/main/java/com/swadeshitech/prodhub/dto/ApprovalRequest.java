package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ApprovalRequest {
    private String serviceId;
    private String comment;
    private MetaDataRequest metaDataRequest;
    private Map<String, Object> metaData;
}
