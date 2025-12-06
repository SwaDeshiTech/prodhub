package com.swadeshitech.prodhub.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.constant.KafkaConstants;
import com.swadeshitech.prodhub.dto.DeploymentRequestResponse;
import com.swadeshitech.prodhub.dto.DeploymentResponse;
import com.swadeshitech.prodhub.dto.DeploymentTemplateResponse;
import com.swadeshitech.prodhub.dto.DeploymentUpdateKafka;
import com.swadeshitech.prodhub.entity.Deployment;
import com.swadeshitech.prodhub.entity.DeploymentSet;
import com.swadeshitech.prodhub.entity.DeploymentTemplate;
import com.swadeshitech.prodhub.enums.DeploymentStatus;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.RunTimeEnvironment;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.deplorch.DeplOrchClient;
import com.swadeshitech.prodhub.integration.kafka.producer.KafkaProducer;
import com.swadeshitech.prodhub.services.DeploymentService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.Base64Util;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DeploymentServiceImpl implements DeploymentService {

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaProducer kafkaProducer;

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
        Deployment deployment = Deployment.builder()
                .status(DeploymentStatus.CREATED)
                .application(deploymentSet.getApplication())
                .metaData(Map.of(
                        "runtimeEnvironment", deploymentSet.getDeploymentProfile().getRunTimeEnvironment().getRunTimeEnvironment(),
                        "deploymentTemplate", deploymentSet.getDeploymentProfile().getRunTimeEnvironment().getDeploymentTemplate())
                )
                .deploymentSet(deploymentSet)
                .build();

        deployment = writeTransactionService.saveDeploymentToRepository(deployment);

        if (CollectionUtils.isEmpty(deploymentSet.getDeployments())) {
            deploymentSet.setDeployments(new ArrayList<>());
        }
        deploymentSet.getDeployments().add(deployment);
        writeTransactionService.saveDeploymentSetToRepository(deploymentSet);

        kafkaProducer.sendMessage(KafkaConstants.DEPLOYMENT_CONFIG_AND_SUBMIT_TOPIC_NAME, deployment.getId());
        return mapEntityToDTO(deployment);
    }

    @Override
    public void generateDeploymentConfig(String deploymentID) {

        List<Deployment> deployments = readTransactionService.findByDynamicOrFilters(Map.of("_id", new ObjectId(deploymentID)), Deployment.class);
        if(CollectionUtils.isEmpty(deployments)) {
            log.error("Deployment could not be found {}", deploymentID);
            throw new CustomException(ErrorCode.DEPLOYMENT_NOT_FOUND);
        }

        String deploymentTemplateName = "DeploymentK8s";

        List<DeploymentTemplate> deploymentTemplates = readTransactionService.findByDynamicOrFilters(Map.of("templateName", deploymentTemplateName), DeploymentTemplate.class);
        if(CollectionUtils.isEmpty(deploymentTemplates)) {
            log.error("Deployment template could not be found {}", deploymentTemplateName);
            throw new CustomException(ErrorCode.DEPLOYMENT_TEMPLATE_COULD_NOT_BE_CREATED);
        }

        Deployment deployment = deployments.getFirst();
        DeploymentTemplate deploymentTemplate = deploymentTemplates.getFirst();

        DeploymentTemplate clonedDeploymentTemplate = new DeploymentTemplate();
        BeanUtils.copyProperties(deploymentTemplate, clonedDeploymentTemplate, "id");
        JsonNode deploymentProfileConfig;
        try {
            deploymentProfileConfig = objectMapper.readTree(Base64Util.convertToPlainText(deployment.getDeploymentSet().getDeploymentProfile().getData()));
        } catch (JsonProcessingException e) {
            log.error("Unable to parse the deployment profile", e);
            throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
        }

        for(DeploymentTemplate.DeploymentStep deploymentStep : clonedDeploymentTemplate.getSteps()) {
            if(!CollectionUtils.isEmpty(deploymentStep.getParams())) {
                Map<String, Object> configs = new HashMap<>();
                for(String key : deploymentStep.getParams()) {
                    if(ObjectUtils.isEmpty(deploymentProfileConfig.path(key))) {
                        configs.put(key, "");
                    } else {
                        configs.put(key, deploymentProfileConfig.path(key).asText());
                    }
                }
                deploymentStep.setValues(configs);
            }
            deploymentStep.setStatus(DeploymentStatus.IN_PROGRESS);
        }

        if (deployment.getMetaData().get("runtimeEnvironment").toString().equals(RunTimeEnvironment.K8s.getRunTimeEnvironment())) {
            deployment.getMetaData().put("k8sClusterId", deploymentProfileConfig.path("k8sClusterName").asText());
        }

        deployment.setStatus(DeploymentStatus.IN_PROGRESS);
        deployment.setDeploymentTemplate(clonedDeploymentTemplate);
        writeTransactionService.saveDeploymentToRepository(deployment);
    }

    @Override
    public void submitDeploymentRequest(String deploymentID) {
        Mono<com.swadeshitech.prodhub.integration.deplorch.DeploymentResponse> responseMono =
                deplOrchClient.triggerDeployment(deploymentID);
        com.swadeshitech.prodhub.integration.deplorch.DeploymentResponse response = responseMono.blockOptional().get();
        log.info("Printing the response {}", response);
    }

    @Override
    public void updateDeploymentStepStatus(DeploymentUpdateKafka deploymentUpdateKafka) {
        List<Deployment> deployments = readTransactionService.findByDynamicOrFilters(Map.of("_id", new ObjectId(deploymentUpdateKafka.getDeploymentRequestId())), Deployment.class);
        if(CollectionUtils.isEmpty(deployments)) {
            log.error("Deployment could not be found {}", deploymentUpdateKafka.getDeploymentRequestId());
            throw new CustomException(ErrorCode.DEPLOYMENT_NOT_FOUND);
        }

        Deployment deployment = deployments.getFirst();

        for(DeploymentTemplate.DeploymentStep step : deployment.getDeploymentTemplate().getSteps()) {
            if(step.getStepName().equalsIgnoreCase(deploymentUpdateKafka.getStepName())) {
                step.setStatus(DeploymentStatus.valueOf(deploymentUpdateKafka.getStatus()));
                if(CollectionUtils.isEmpty(step.getMetadata())) {
                    step.setMetadata(new HashMap<>());
                }
                step.getMetadata().put("timestamp", deploymentUpdateKafka.getTimestamp());
                step.getMetadata().put("details", deploymentUpdateKafka.getDetails());
                updateDeploymentStatus(deployment);
                break;
            }
        }

        writeTransactionService.saveDeploymentToRepository(deployment);
    }

    @Override
    public DeploymentResponse getDeploymentDetails(String deploymentId) {

        List<Deployment> deployments = readTransactionService.findByDynamicOrFilters(Map.of("_id", new ObjectId(deploymentId)), Deployment.class);
        if(CollectionUtils.isEmpty(deployments)) {
            log.error("Deployment could not be found {}", deploymentId);
            throw new CustomException(ErrorCode.DEPLOYMENT_NOT_FOUND);
        }

        Deployment deployment = deployments.getFirst();

        return DeploymentResponse.builder()
                .id(deployment.getId())
                .applicationId(deployment.getApplication().getId())
                .deploymentSetId(deployment.getDeploymentSet().getId())
                .status(deployment.getStatus().getMessage())
                .deploymentTemplateResponse(DeploymentTemplateResponse.mapDTOToEntity(deployment.getDeploymentTemplate()))
                .createdBy(deployment.getCreatedBy())
                .createdTime(deployment.getCreatedTime())
                .lastModifiedBy(deployment.getLastModifiedBy())
                .lastModifiedTime(deployment.getLastModifiedTime())
                .build();
    }

    private void updateDeploymentStatus(Deployment deployment) {

        int stepExecutedSuccessfully = 0, totalStep = deployment.getDeploymentTemplate().getSteps().size();

        for(DeploymentTemplate.DeploymentStep deploymentStep : deployment.getDeploymentTemplate().getSteps()) {
            if (deploymentStep.getStatus().equals(DeploymentStatus.COMPLETED)) {
                stepExecutedSuccessfully++;
            } else if (deploymentStep.getStatus().equals(DeploymentStatus.FAILED)){
                deployment.setStatus(DeploymentStatus.FAILED);
                return;
            }
        }
        if(stepExecutedSuccessfully == totalStep) {
            deployment.setStatus(DeploymentStatus.COMPLETED);
        }
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
