package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.integration.deplorch.DeploymentPodResponse;
import org.springframework.stereotype.Component;

@Component
public interface DeploymentService {

    DeploymentRequestResponse triggerDeployment(String deploymentSetID);

    void generateDeploymentConfig(String deploymentID);

    void submitDeploymentRequest(String deploymentID);

    void updateDeploymentStepStatus(DeploymentUpdateKafka deploymentUpdateKafka);

    DeploymentResponse getDeploymentDetails(String deploymentId);

    DeploymentPodResponse getDeployedPodDetails(String deploymentId);

    PaginatedResponse<DeploymentRequestResponse> getAllDeployments(Integer page, Integer size, String sortBy, String order);
}
