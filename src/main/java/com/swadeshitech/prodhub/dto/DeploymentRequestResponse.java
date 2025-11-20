package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class DeploymentRequestResponse extends BaseResponse {
    private String deploymentSetId;
    private String status;
    private String runId;
}
