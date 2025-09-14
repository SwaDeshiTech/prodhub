package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class ApprovalUpdateRequest {
    private String name;
    private String status;
    private String comments;
}
