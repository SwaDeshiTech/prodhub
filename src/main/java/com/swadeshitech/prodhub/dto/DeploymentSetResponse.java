package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
public class DeploymentSetResponse extends BaseResponse {
    private String id;
    private String serviceName;
    private String status;
    private String deploymentProfileName;
    private String buildProfileName;
    private Map<String, String> metaData;
    private String approvalId;
}
