package com.swadeshitech.prodhub.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeploymentUpdateKafka {
    private String deploymentRequestId;
    private String stepName;
    private String status;
    private String timestamp;
    private String details;
}
