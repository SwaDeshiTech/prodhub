package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class DeploymentSetUpdateRequest {
    private String approvalStage;
    private String status;
    private String comments;
}
