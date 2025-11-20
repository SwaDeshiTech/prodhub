package com.swadeshitech.prodhub.integration.deplorch;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DeploymentRequest {
    private String deploymentId;
}
