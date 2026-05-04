package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class DeploymentResponse extends BaseResponse {
    private String deploymentID;
    private String status;
    private Map<String, String> metaData;
    private TemplateResponse deploymentTemplateResponse;
    private String deploymentSetId;
    private String applicationId;
}
