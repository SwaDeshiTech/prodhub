package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
public class DeploymentSetResponse extends BaseResponse {
    private String id;
    private String serviceName;
    private String status;
    private String deploymentProfileName;
    private String buildProfileName;
    private Map<String, String> metaData;
    private String approvalId;
    private List<PipelineExecutionDetailsDTO> pipelineExecutions;
}
