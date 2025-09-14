package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class ApprovalRequestFilter {
    private String serviceId;
    private String profileId;
    private String status;
}
