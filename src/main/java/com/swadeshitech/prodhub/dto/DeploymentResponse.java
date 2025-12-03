package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
public class DeploymentResponse extends BaseResponse {
    private String deploymentID;
    private String status;
    private Map<String, String> metaData;
    private DeploymentTemplateResponse deploymentTemplateResponse;
    private String deploymentSetId;
    private String applicationId;
}
