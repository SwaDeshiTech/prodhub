package com.swadeshitech.prodhub.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.constant.KafkaConstants;
import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.enums.DeploymentStatus;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.RunTimeEnvironment;
import com.swadeshitech.prodhub.enums.StepExecutionStatus;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.deplorch.DeplOrchClient;
import com.swadeshitech.prodhub.integration.deplorch.DeploymentPodResponse;
import com.swadeshitech.prodhub.integration.kafka.producer.KafkaProducer;
import com.swadeshitech.prodhub.services.CredentialProviderService;
import com.swadeshitech.prodhub.services.DeploymentService;
import com.swadeshitech.prodhub.services.MetadataService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.Base64Util;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.swadeshitech.prodhub.constant.Constants.NAMESPACE_KEY;

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

    @Autowired
    UserServiceImpl userService;

    @Autowired
    MetadataService metadataService;

    @Autowired
    CredentialProviderService credentialProviderService;

    @Override
    public DeploymentRequestResponse triggerDeployment(String deploymentSetID) {

        List<DeploymentSet> deploymentSets = readTransactionService
                .findByDynamicOrFilters(Map.of("_id", new ObjectId(deploymentSetID)), DeploymentSet.class);
        if (CollectionUtils.isEmpty(deploymentSets)) {
            log.error("Deployment set could not be found {}", deploymentSetID);
            throw new CustomException(ErrorCode.DEPLOYMENT_SET_NOT_FOUND);
        }

        DeploymentSet deploymentSet = deploymentSets.getFirst();
        Deployment deployment = Deployment.builder()
                .status(DeploymentStatus.CREATED)
                .application(deploymentSet.getApplication())
                .metaData(Map.of(
                        "runtimeEnvironment",
                        deploymentSet.getDeploymentProfile().getRunTimeEnvironment().getRunTimeEnvironment(),
                        "deploymentTemplate",
                        deploymentSet.getDeploymentProfile().getRunTimeEnvironment().getDeploymentTemplate(),
                        "releaseName",
                        deploymentSet.getDeploymentProfile().getApplication().getName() + "-"
                                + deploymentSet.getDeploymentProfile().extractMetaDataName(),
                        "imageTag", deploymentSet.getReleaseCandidate().getMetaData().get("dockerImageHashValue")))
                .deploymentSet(deploymentSet)
                .build();

        deployment = writeTransactionService.saveDeploymentToRepository(deployment);

        // Note: DeploymentSet now uses pipelineExecutions instead of deployments
        // This method is deprecated - deployments should be triggered via pipeline execution
        writeTransactionService.saveDeploymentSetToRepository(deploymentSet);

        kafkaProducer.sendMessage(KafkaConstants.DEPLOYMENT_CONFIG_AND_SUBMIT_TOPIC_NAME, deployment.getId());
        return mapEntityToDTO(deployment);
    }

    @Override
    public void generateDeploymentConfig(String deploymentID) {

        String deploymentTemplateName = "DeploymentK8s";

        List<Template> templates = readTransactionService
                .findByDynamicOrFilters(Map.of("templateName", deploymentTemplateName), Template.class);
        if (CollectionUtils.isEmpty(templates)) {
            log.error("Deployment template could not be found {}", deploymentTemplateName);
            throw new CustomException(ErrorCode.DEPLOYMENT_TEMPLATE_COULD_NOT_BE_CREATED);
        }

        Deployment deployment = findDeployment(deploymentID);
        Template template = templates.getFirst();

        Template clonedTemplate = new Template();
        BeanUtils.copyProperties(template, clonedTemplate, "id");
        JsonNode deploymentProfileConfig;
        try {
            deploymentProfileConfig = objectMapper.readTree(
                    Base64Util.convertToPlainText(deployment.getDeploymentSet().getDeploymentProfile().getData()));
        } catch (JsonProcessingException e) {
            log.error("Unable to parse the deployment profile", e);
            throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
        }

        for (Template.Step deploymentStep : clonedTemplate.getSteps()) {
            if (!CollectionUtils.isEmpty(deploymentStep.getParams()) && !deploymentStep.isSkipStep()) {
                Map<String, Object> configs = new HashMap<>();
                for (Map.Entry<String, Template.Step.TemplateStepParam> key : deploymentStep.getParams().entrySet()) {
                    if (ObjectUtils.isEmpty(deploymentProfileConfig.path(key.getKey()))) {
                        configs.put(key.getKey(), "");
                    } else {
                        configs.put(key.getKey(), deploymentProfileConfig.path(key.getKey()).asText());
                    }
                }
                deploymentStep.setValues(configs);
            }
            deploymentStep.setMetadata(new HashMap<>());
            deploymentStep.setStatus(StepExecutionStatus.IN_PROGRESS);
        }

        if (RunTimeEnvironment.K8s.getRunTimeEnvironment()
                .equals(deployment.getMetaData().get("runtimeEnvironment").toString())) {
            deployment.getMetaData().put("k8sClusterId", deploymentProfileConfig.path("k8sClusterName").asText());
            deployment.getMetaData().put("dockerContainerRegistry",
                    deploymentProfileConfig.path("dockerContainerRegistry").asText());
        }

        deployment.getMetaData().put("namespace", deploymentProfileConfig.path("namespace").asText());
        deployment.setStatus(DeploymentStatus.IN_PROGRESS);
        deployment.setTemplate(clonedTemplate);
        writeTransactionService.saveDeploymentToRepository(deployment);
    }

    @Override
    public void submitDeploymentRequest(String deploymentID) {
        Mono<com.swadeshitech.prodhub.integration.deplorch.DeploymentResponse> responseMono = deplOrchClient
                .triggerDeployment(deploymentID);
        com.swadeshitech.prodhub.integration.deplorch.DeploymentResponse response = responseMono.blockOptional().get();
        log.info("Printing the response {}", response);
    }

    @Override
    public void updateDeploymentStepStatus(DeploymentUpdateKafka deploymentUpdateKafka) {

        Deployment deployment = findDeployment(deploymentUpdateKafka.getDeploymentRequestId());
        boolean isDeploymentStepFailed = false;

        if (DeploymentStatus.valueOf(deploymentUpdateKafka.getStatus()).equals(DeploymentStatus.FAILED)) {
            isDeploymentStepFailed = true;
        }
        for (Template.Step step : deployment.getTemplate().getSteps()) {
            if (step.getStepName().equalsIgnoreCase(deploymentUpdateKafka.getStepName())) {
                step.setStatus(StepExecutionStatus.valueOf(deploymentUpdateKafka.getStatus()));
                step.getMetadata().put("timestamp", deploymentUpdateKafka.getTimestamp());
                step.getMetadata().put("details", deploymentUpdateKafka.getDetails());
                updateDeploymentStatus(deployment);
                if(isDeploymentStepFailed) {
                    for (Template.Step innerStep : deployment.getTemplate().getSteps()) {
                        if(innerStep.getOrder() > step.getOrder()) {
                            innerStep.setStatus(StepExecutionStatus.SKIPPED);
                        }
                    }
                }
            }
        }

        writeTransactionService.saveDeploymentToRepository(deployment);
    }

    @Override
    public DeploymentResponse getDeploymentDetails(String deploymentId) {

        Deployment deployment = findDeployment(deploymentId);

        DeploymentResponse deploymentResponse = DeploymentResponse.builder()
                .id(deployment.getId())
                .applicationId(deployment.getApplication().getName())
                .status(deployment.getStatus().name())
                .deploymentTemplateResponse(TemplateResponse.mapDTOToEntity(deployment.getTemplate()))
                .createdBy(deployment.getCreatedBy())
                .createdTime(deployment.getCreatedTime())
                .lastModifiedBy(deployment.getLastModifiedBy())
                .lastModifiedTime(deployment.getLastModifiedTime())
                .build();

        if (deployment.getDeploymentSet() != null) {
            deploymentResponse.setDeploymentSetId(deployment.getDeploymentSet().getId());
        }

        return deploymentResponse;
    }

    @Override
    public DeploymentPodResponse getDeployedPodDetails(String deploymentId, String ephemeralEnvironment) {

        String k8sClusterId;
        String namespace;

        if (StringUtils.hasText(ephemeralEnvironment)) {
            // For ephemeral environments, fetch ephemeral environment to get k8s cluster ID
            List<com.swadeshitech.prodhub.entity.EphemeralEnvironment> ephemeralEnvironments =
                readTransactionService.findByDynamicOrFilters(
                    Map.of("name", ephemeralEnvironment),
                    com.swadeshitech.prodhub.entity.EphemeralEnvironment.class
                );
            
            if (CollectionUtils.isEmpty(ephemeralEnvironments)) {
                log.error("Ephemeral environment could not be found with name {}", ephemeralEnvironment);
                throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_NOT_FOUND);
            }

            com.swadeshitech.prodhub.entity.EphemeralEnvironment ephemeralEnv = ephemeralEnvironments.getFirst();
            
            // Get k8s cluster ID from ephemeral environment metadata
            if (ephemeralEnv.getMetaData() == null || !ephemeralEnv.getMetaData().containsKey("k8sClusterId")) {
                log.error("K8s cluster ID not found in ephemeral environment metadata for {}", ephemeralEnvironment);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            
            k8sClusterId = String.valueOf(ephemeralEnv.getMetaData().get("k8sClusterId"));
            namespace = ephemeralEnvironment;
        } else {
            PipelineExecution pipelineExecution = findPipelineExecutionById(deploymentId);
            if (pipelineExecution == null) {
                throw new CustomException(ErrorCode.DEPLOYMENT_NOT_FOUND);
            }
            String deploymentProfileId = String.valueOf(pipelineExecution.getMetaData().get("metaDataId"));
            List<Metadata> deploymentProfile = readTransactionService.findMetaDataByFilters(Map.of("_id",
                    new ObjectId(deploymentProfileId)));
            if(CollectionUtils.isEmpty(deploymentProfile)) {
                log.error("Deployment profile could not be found {}", deploymentProfileId);
                throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
            }
            k8sClusterId = metadataService.extractValueFromKey(deploymentProfile.getFirst(), "k8sClusterName");
            namespace = metadataService.extractValueFromKey(deploymentProfile.getFirst(), "namespace");
        }

        DeploymentPodResponse deploymentPodResponse = deplOrchClient
                .getDeployedPodDetails(k8sClusterId, namespace)
                .block();

        if (Objects.isNull(deploymentPodResponse)) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        List<CredentialProvider> credentialProviders = readTransactionService.findCredentialProviderByFilters(
                Map.of("_id", new ObjectId(k8sClusterId)));
        if (credentialProviders.isEmpty()) {
            log.error("K8s cluster could not be found in vault {}", k8sClusterId);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_NOT_FOUND);
        }

        deploymentPodResponse.setClusterId(credentialProviders.getFirst().getName());

        return deploymentPodResponse;
    }

    @Override
    public PaginatedResponse<DeploymentRequestResponse> getAllDeployments(Integer page, Integer size, String sortBy,
            String order, String ephemeralEnvironment) {

        log.info("Fetching deployments for page {} with size {}", page, size);
        User user = userService.extractUserFromContext();

        Map<String, Object> filters = new HashMap<>();
        if (StringUtils.hasText(ephemeralEnvironment)) {
            filters.put("metaData.ephemeralEnvironment", ephemeralEnvironment);
        } else {
            filters.put("createdBy", user.getEmailId());
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<Deployment> deploymentsPage = readTransactionService.findByDynamicOrFiltersPaginated(
                filters,
                Deployment.class,
                page,
                size,
                sortBy,
                direction);
        if (deploymentsPage.isEmpty()) {
            log.warn("No deployment found");
            throw new CustomException(ErrorCode.DEPLOYMENT_NOT_FOUND);
        }

        List<DeploymentRequestResponse> dtoList = deploymentsPage.getContent().stream()
                .map(this::mapEntityToDTO)
                .toList();

        return PaginatedResponse.<DeploymentRequestResponse>builder()
                .content(dtoList)
                .pageNumber(deploymentsPage.getNumber())
                .pageSize(deploymentsPage.getSize())
                .totalElements(deploymentsPage.getTotalElements())
                .totalPages(deploymentsPage.getTotalPages())
                .isLast(deploymentsPage.isLast())
                .build();
    }

    @Override
    public Deployment triggerDeploymentForEphemeralEnvironment(EphemeralEnvironment ephemeralEnvironment,
            Metadata deploymentProfile, ReleaseCandidate releaseCandidate) {
        return writeTransactionService
                .saveDeploymentToRepository(generateDeploymentConfigForEphemeralEnvironment(ephemeralEnvironment,
                        releaseCandidate, deploymentProfile));
    }

    private Deployment generateDeploymentConfigForEphemeralEnvironment(EphemeralEnvironment ephemeralEnvironment,
            ReleaseCandidate releaseCandidate, Metadata deploymentProfile) {
        String deploymentTemplateName = "DeploymentK8s";

        List<Template> templates = readTransactionService
                .findByDynamicOrFilters(Map.of("templateName", deploymentTemplateName), Template.class);
        if (CollectionUtils.isEmpty(templates)) {
            log.error("Deployment template could not be found {}", deploymentTemplateName);
            throw new CustomException(ErrorCode.DEPLOYMENT_TEMPLATE_COULD_NOT_BE_CREATED);
        }

        Template template = templates.getFirst();
        Template clonedTemplate = new Template();
        BeanUtils.copyProperties(template, clonedTemplate, "id");

        JsonNode deploymentProfileConfig;
        try {
            deploymentProfileConfig = objectMapper.readTree(Base64Util.convertToPlainText(deploymentProfile.getData()));
        } catch (JsonProcessingException e) {
            log.error("Unable to parse the deployment profile", e);
            throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
        }

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("runtimeEnvironment", deploymentProfile.getRunTimeEnvironment().getRunTimeEnvironment());
        configMap.put("deploymentTemplate", deploymentProfile.getRunTimeEnvironment().getDeploymentTemplate());
        
        String appName = deploymentProfile.getApplication().getName().toLowerCase();
        String envName = ephemeralEnvironment.getName().toLowerCase();
        configMap.put("releaseName", envName + "-" + appName);
        
        configMap.put("imageTag", releaseCandidate.getMetaData().get("dockerImageHashValue"));
        configMap.put("ephemeralEnvironment", releaseCandidate.getEphemeralEnvironmentId());

        Deployment deployment = Deployment.builder()
                .status(DeploymentStatus.CREATED)
                .application(deploymentProfile.getApplication())
                .template(clonedTemplate)
                .metaData(configMap)
                .build();

        if (!CollectionUtils.isEmpty(clonedTemplate.getSteps())) {
            for (Template.Step deploymentStep : clonedTemplate.getSteps()) {
                if (deploymentStep.isSkipStep()) {
                    continue;
                }
                
                Map<String, Object> configs = new HashMap<>();
                String registryId = null;
                
                for (Map.Entry<String, Template.Step.TemplateStepParam> entry : deploymentStep.getParams().entrySet()) {
                    String key = entry.getKey();
                    if ("namespace".equalsIgnoreCase(key)) {
                        configs.put(key, ephemeralEnvironment.getName().toLowerCase());
                    } else if ("name".equalsIgnoreCase(key)) {
                        configs.put(key, envName + "-" + appName);
                    } else if ("imageTag".equalsIgnoreCase(key)) {
                        configs.put(key, releaseCandidate.getMetaData().get("dockerImageHashValue"));
                    } else if ("applicationName".equalsIgnoreCase(key) || "serviceName".equalsIgnoreCase(key)) {
                        configs.put(key, appName);
                    } else if ("dockerContainerRegistry".equalsIgnoreCase(key)) {
                        registryId = deploymentProfileConfig.path(key).asText();
                        configs.put(key, registryId);
                    } else if (ObjectUtils.isEmpty(deploymentProfileConfig.path(key))) {
                        configs.put(key, "");
                    } else {
                        configs.put(key, deploymentProfileConfig.path(key).asText());
                    }
                }
                
                // Resolve registry URL
                if (StringUtils.hasText(registryId)) {
                    String registryURL = credentialProviderService.extractRegistryURL(registryId);
                    if (StringUtils.hasText(registryURL)) {
                        configs.put("registryURL", registryURL);
                    }
                }
                
                deploymentStep.setValues(configs);
                deploymentStep.setStatus(StepExecutionStatus.IN_PROGRESS);
            }
        }

        if (RunTimeEnvironment.K8s.getRunTimeEnvironment()
                .equals(deployment.getMetaData().get("runtimeEnvironment").toString())) {
            deployment.getMetaData().put("k8sClusterId", deploymentProfileConfig.path("k8sClusterName").asText());
            deployment.getMetaData().put("dockerContainerRegistry",
                    deploymentProfileConfig.path("dockerContainerRegistry").asText());
        }

        deployment.getMetaData().put("namespace", ephemeralEnvironment.getName());
        deployment.setStatus(DeploymentStatus.IN_PROGRESS);
        deployment.setTemplate(clonedTemplate);

        return deployment;
    }

    private Deployment findDeployment(String deploymentId) {
        List<Deployment> deployments = readTransactionService
                .findByDynamicOrFilters(Map.of("_id", new ObjectId(deploymentId)), Deployment.class);
        if (CollectionUtils.isEmpty(deployments)) {
            log.error("Deployment could not be found {}", deploymentId);
            throw new CustomException(ErrorCode.DEPLOYMENT_NOT_FOUND);
        }
        return deployments.getFirst();
    }

    private PipelineExecution findPipelineExecutionById(String pipelineExecutionId) {
        if (!ObjectId.isValid(pipelineExecutionId)) {
            return null;
        }

        List<PipelineExecution> executions = readTransactionService.findPipelineExecutionsByFilters(
                Map.of("_id", new ObjectId(pipelineExecutionId)));
        if (CollectionUtils.isEmpty(executions)) {
            log.error("Pipeline execution could not be found with id {}", pipelineExecutionId);
            throw new CustomException(ErrorCode.PIPELINE_EXECUTION_COULD_NOT_BE_FOUND);
        }
        return executions.getFirst();
    }

    private void updateDeploymentStatus(Deployment deployment) {

        int stepExecutedSuccessfully = 0, totalStep = deployment.getTemplate().getSteps().size();

        for (Template.Step deploymentStep : deployment.getTemplate().getSteps()) {
            if (StepExecutionStatus.COMPLETED.equals(deploymentStep.getStatus()) || deploymentStep.isSkipStep()) {
                stepExecutedSuccessfully++;
            } else if (deploymentStep.getStatus().equals(StepExecutionStatus.FAILED)) {
                deployment.setStatus(DeploymentStatus.FAILED);
                return;
            }
        }
        if (stepExecutedSuccessfully == totalStep) {
            deployment.setStatus(DeploymentStatus.COMPLETED);
        }
    }

    private DeploymentRequestResponse mapEntityToDTO(Deployment deployment) {
        return DeploymentRequestResponse.builder()
                .id(deployment.getId())
                .runId(deployment.getId())
                .status(deployment.getStatus().name())
                .createdBy(deployment.getCreatedBy())
                .createdTime(deployment.getCreatedTime())
                .lastModifiedBy(deployment.getLastModifiedBy())
                .lastModifiedTime(deployment.getLastModifiedTime())
                .build();
    }
}
