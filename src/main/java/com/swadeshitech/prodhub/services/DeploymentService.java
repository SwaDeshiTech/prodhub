package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.DeploymentRequestResponse;
import org.springframework.stereotype.Component;

@Component
public interface DeploymentService {

    DeploymentRequestResponse triggerDeployment(String deploymentSetID);

    void generateDeploymentConfig(String deploymentID);

    void submitDeploymentRequest(String deploymentID);
}
