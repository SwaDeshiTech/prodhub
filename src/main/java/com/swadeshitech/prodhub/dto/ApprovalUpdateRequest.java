package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ApprovalUpdateRequest {
    private String name;
    private String status;
    private String comments;
    private Map<String, String> metaData;
}
