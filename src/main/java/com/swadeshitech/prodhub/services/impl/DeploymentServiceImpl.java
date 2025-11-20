package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.DeploymentRequestResponse;
import com.swadeshitech.prodhub.entity.Deployment;
import com.swadeshitech.prodhub.entity.DeploymentRun;
import com.swadeshitech.prodhub.entity.DeploymentSet;
import com.swadeshitech.prodhub.enums.DeploymentRunStatus;
import com.swadeshitech.prodhub.enums.DeploymentStatus;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.deplorch.DeplOrchClient;
import com.swadeshitech.prodhub.integration.deplorch.DeploymentRequest;
import com.swadeshitech.prodhub.integration.deplorch.DeploymentResponse;
import com.swadeshitech.prodhub.services.DeploymentService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DeploymentServiceImpl implements DeploymentService {

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    DeplOrchClient deplOrchClient;

    @Override
    public DeploymentRequestResponse triggerDeployment(String deploymentSetID) {

        List<DeploymentSet> deploymentSets = readTransactionService.findByDynamicOrFilters(Map.of("_id", new ObjectId(deploymentSetID)), DeploymentSet.class);
        if(CollectionUtils.isEmpty(deploymentSets)) {
            log.error("Deployment set could not be found {}", deploymentSetID);
            throw new CustomException(ErrorCode.DEPLOYMENT_SET_NOT_FOUND);
        }

        DeploymentSet deploymentSet = deploymentSets.getFirst();

        DeploymentRun deploymentRun = DeploymentRun.builder()
                .deploymentRunStatus(DeploymentRunStatus.CREATED)
                .application(deploymentSet.getApplication())
                .metaData(deploymentSet.getDeploymentProfile())
                .build();

        deploymentRun = writeTransactionService.saveDeploymentRunToRepository(deploymentRun);

        Deployment deployment = Deployment.builder()
                .status(DeploymentStatus.CREATED)
                .application(deploymentSet.getApplication())
                .referenceID(deploymentSet.getUuid())
                .deploymentRuns(Collections.singletonList(deploymentRun))
                .metaData(Map.of(
                        "runtimeEnvironment", deploymentRun.getMetaData().getRunTimeEnvironment().getRunTimeEnvironment(),
                        "deploymentTemplate", deploymentRun.getMetaData().getRunTimeEnvironment().getDeploymentTemplate())
                )
                .build();

        deployment = writeTransactionService.saveDeploymentToRepository(deployment);

        DeploymentResponse deploymentResponse = deplOrchClient.triggerDeployment(DeploymentRequest.builder()
                        .deploymentId(deployment.getId())
                .build()).block();

        deployment.setStatus(DeploymentStatus.IN_PROGRESS);
        //deployment.getMetaData().put("DeplOrchID", deploymentResponse.getId());

        deployment = writeTransactionService.saveDeploymentToRepository(deployment);

        return mapEntityToDTO(deployment);
    }

    private DeploymentRequestResponse mapEntityToDTO(Deployment deployment) {
        return DeploymentRequestResponse.builder()
                .deploymentSetId(deployment.getId())
                .runId(deployment.getId())
                .status(deployment.getStatus().name())
                .createdBy(deployment.getCreatedBy())
                .createdTime(deployment.getCreatedTime())
                .lastModifiedBy(deployment.getLastModifiedBy())
                .lastModifiedTime(deployment.getLastModifiedTime())
                .build();
    }
}
