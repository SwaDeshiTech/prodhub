package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.DeploymentSetRequest;
import com.swadeshitech.prodhub.dto.DeploymentSetResponse;
import com.swadeshitech.prodhub.dto.DeploymentSetUpdateRequest;
import com.swadeshitech.prodhub.enums.DeploymentSetStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DeploymentSetService {

    String createDeploymentSet(DeploymentSetRequest request);

    DeploymentSetResponse getDeploymentSetDetails(String id);

    List<DeploymentSetResponse> getDeploymentResponseList();

    void updateDeploymentSet(String id, DeploymentSetUpdateRequest request);

    void updateDeploymentSetStatus(String deploymentSetIDUUID);
}
