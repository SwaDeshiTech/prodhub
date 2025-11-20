package com.swadeshitech.prodhub.integration.deplorch;

import lombok.Data;

import java.util.Map;

@Data
public class DeploymentResponse {
    private String id;
    private String referenceID;
    private String runtimeEnvironment;
    private String deploymentTemplate;
    private Map<String, String> meta;
    private String status;
}
