package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.Deployment;
import com.swadeshitech.prodhub.entity.EphemeralEnvironment;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.ReleaseCandidate;
import com.swadeshitech.prodhub.integration.deplorch.DeploymentPodResponse;
import org.springframework.stereotype.Component;

@Component
public interface DeploymentService {

    DeploymentRequestResponse triggerDeployment(String deploymentSetID);

    void generateDeploymentConfig(String deploymentID);

    void submitDeploymentRequest(String deploymentID);

    void updateDeploymentStepStatus(DeploymentUpdateKafka deploymentUpdateKafka);

    DeploymentResponse getDeploymentDetails(String deploymentId);

    DeploymentPodResponse getDeployedPodDetails(String deploymentId, String ephemeralEnvironment);

    PaginatedResponse<DeploymentRequestResponse> getAllDeployments(Integer page, Integer size, String sortBy, String order, String ephemeralEnvironment);

    Deployment triggerDeploymentForEphemeralEnvironment(EphemeralEnvironment ephemeralEnvironment, Metadata deploymentProfile, ReleaseCandidate releaseCandidate);
}
