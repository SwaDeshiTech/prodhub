package com.swadeshitech.prodhub.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.dto.PaginatedResponse;
import com.swadeshitech.prodhub.dto.PipelineExecutionDetailsDTO;
import com.swadeshitech.prodhub.dto.PipelineExecutionRequest;
import com.swadeshitech.prodhub.dto.StageExecutionDTO;
import com.swadeshitech.prodhub.dto.TemplateResponse;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.entity.PipelineTemplate;
import com.swadeshitech.prodhub.entity.Template;
import com.swadeshitech.prodhub.enums.*;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerResponse;
import com.swadeshitech.prodhub.integration.deplorch.DeplOrchClient;
import com.swadeshitech.prodhub.integration.deplorch.DeploymentResponse;
import com.swadeshitech.prodhub.integration.kafka.producer.KafkaProducer;
import com.swadeshitech.prodhub.provider.BuildProvider;
import com.swadeshitech.prodhub.services.CredentialProviderService;
import com.swadeshitech.prodhub.services.PipelineService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.Base64Util;
import com.swadeshitech.prodhub.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class PipelineServiceImpl implements PipelineService {

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaProducer kafkaProducer;

    @Value("${spring.kafka.topic.pipeline-execution}")
    String pipelineExecutionTopicName;

    @Autowired
    BuildProvider buildProvider;

    @Autowired
    CredentialProviderService credentialProviderService;

    @Autowired
    com.swadeshitech.prodhub.integration.cicaptain.config.CiCaptainClient ciCaptainClient;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public String schedulePipelineExecution(PipelineExecutionRequest request) {

        PipelineExecution pipelineExecution = createPipelineExecution(request);

        kafkaProducer.sendMessage(pipelineExecutionTopicName, pipelineExecution.getId());

        return pipelineExecution.getId();
    }

    @Override
    public PipelineExecution createPipelineExecution(PipelineExecutionRequest request) {

        // Fetch metadata to extract pipelineTemplateId
        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(
                Map.of("_id", new ObjectId(request.getMetaDataID())));
        if (CollectionUtils.isEmpty(metadataList)) {
            log.error("Metadata could not be found for ID {}", request.getMetaDataID());
            throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
        }
        Metadata metadata = metadataList.getFirst();

        String pipelineTemplateId = extractPipelineTemplateIdFromMetadata(metadata);

        PipelineTemplate pipelineTemplate = fetchPipelineTemplate(
                Map.of("_id", new ObjectId(pipelineTemplateId))
        );

        PipelineExecution pipelineExecution = new PipelineExecution();
        pipelineExecution.setStageExecutions(createStages(request, pipelineTemplate, request.getMetaDataID()));
        pipelineExecution.setPipelineTemplate(pipelineTemplate);
        pipelineExecution.setStatus(PipelineStatus.PENDING);
        pipelineExecution.setMetaData(new HashMap<>(Map.of(
                "metaDataId", request.getMetaDataID(),
                "serviceId", metadata.getApplication().getId()
        )));

        return writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
    }

    private String extractPipelineTemplateIdFromMetadata(Metadata metadata) {
        try {
            JsonNode metadataJson = objectMapper.readTree(
                    Base64Util.convertToPlainText(metadata.getData()));
            JsonNode pipelineTemplateIdNode = metadataJson.path("pipelineTemplateId");
            if (pipelineTemplateIdNode.isMissingNode() || !StringUtils.hasText(pipelineTemplateIdNode.asText())) {
                log.error("pipelineTemplateId not found in metadata {}", metadata.getId());
                throw new CustomException(ErrorCode.PIPELINE_TEMPLATE_COULD_NOT_BE_FOUND);
            }
            return pipelineTemplateIdNode.asText();
        } catch (JsonProcessingException e) {
            log.error("Unable to parse metadata data for ID {}", metadata.getId(), e);
            throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
        }
    }

    @Override
    public void startPipelineExecution(PipelineExecution pipelineExecution) {
        pipelineExecution.getStageExecutions().sort((o1, o2) -> o1.getOrder() - o2.getOrder());
        
        // Set pipeline status to IN_PROGRESS when starting
        pipelineExecution.setStatus(PipelineStatus.IN_PROGRESS);
        writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
        log.info("Pipeline execution {} started and set to IN_PROGRESS", pipelineExecution.getId());
        
        // Only trigger the first pending stage
        for (PipelineExecution.StageExecution stageExecution : pipelineExecution.getStageExecutions()) {
            if (Objects.nonNull(stageExecution) && stageExecution.getStatus() == PipelineStepExecutionStatus.PENDING) {
                log.info("Triggering stage: {} for pipeline execution: {}", stageExecution.getStageName(), pipelineExecution.getId());
                
                switch (stageExecution.getStageName().toLowerCase()) {
                    case "build":
                        handleBuildPipelineTemplate(pipelineExecution, stageExecution);
                        break;
                    case "deployment":
                        handleDeploymentPipelineTemplate(pipelineExecution, stageExecution);
                        break;
                    default:
                        log.error("Unknown pipeline template type for pipeline execution {}", pipelineExecution.getId());
                }
                return; // Exit after triggering the first pending stage
            }
        }
        
        log.info("No pending stages found for pipeline execution: {}", pipelineExecution.getId());
    }

    private List<PipelineExecution.StageExecution> createStages(PipelineExecutionRequest request,
            PipelineTemplate pipelineTemplate, String metaDataId) {
        List<PipelineExecution.StageExecution> stages = new ArrayList<>();

        for (PipelineTemplate.StageDefinition stageDefinition : pipelineTemplate.getStages()) {
            PipelineExecution.StageExecution stageExecution = PipelineExecution.StageExecution.builder()
                    .id(UuidUtil.generateRandomUuid())
                    .stageName(stageDefinition.getName())
                    .startTime(LocalDateTime.now())
                    .stopOnFailure(stageDefinition.isStopOnFailure())
                    .order(stageDefinition.getOrder())
                    .status(PipelineStepExecutionStatus.PENDING)
                    .template(generateTemplateForPipeline(request, stageDefinition.getTemplateName(), metaDataId))
                    .build();
            if (stageDefinition.getName().equalsIgnoreCase("init")) {
                stageExecution.setStatus(PipelineStepExecutionStatus.SUCCESS);
            }
            stages.add(stageExecution);
        }

        return stages;
    }

    private Template generateTemplateForPipeline(PipelineExecutionRequest request, String templateName,
            String metaDataId) {

        if (!StringUtils.hasText(templateName)) {
            return null;
        }

        List<Template> templates = readTransactionService.findByDynamicOrFilters(
                Map.of("templateName", templateName),
                Template.class);
        if (CollectionUtils.isEmpty(templates)) {
            log.error("Template could not be found {}", templateName);
            throw new CustomException(ErrorCode.PIPELINE_TEMPLATE_COULD_NOT_BE_FOUND);
        }
        Template template = templates.getFirst();

        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(Map.of(
                "_id", new ObjectId(metaDataId)));
        if (CollectionUtils.isEmpty(metadataList)) {
            log.error("Metadata could not be found {}", metaDataId);
        }
        Metadata metadata = metadataList.getFirst();

        Template clonedTemplate = new Template();
        BeanUtils.copyProperties(template, clonedTemplate, "id");

        JsonNode profileConfig;
        try {
            profileConfig = objectMapper.readTree(
                    Base64Util.convertToPlainText(metadata.getData()));
        } catch (JsonProcessingException e) {
            log.error("Unable to parse the deployment profile", e);
            throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
        }

        for (Template.Step step : clonedTemplate.getSteps()) {
            if (step.getStepName().equalsIgnoreCase("init")) {
                step.setStatus(StepExecutionStatus.COMPLETED);
            } else {
                if (!CollectionUtils.isEmpty(step.getParams()) && !step.isSkipStep()) {
                    if (step.getStepName().equalsIgnoreCase("build")) {
                        handleParamGenerationForBuildStep(request, step, profileConfig, metadata);
                    } else {
                        Map<String, Object> configs = new HashMap<>();
                        for (Map.Entry<String, Template.Step.TemplateStepParam> itr : step.getParams().entrySet()) {
                            if (ObjectUtils.isEmpty(profileConfig.path(itr.getKey()))) {
                                configs.put(itr.getKey(), "");
                            } else {
                                configs.put(itr.getKey(), profileConfig.path(itr.getKey()).asText());
                            }
                        }
                        step.setValues(configs);
                        step.setMetadata(new HashMap<>());
                        step.setStatus(StepExecutionStatus.IN_PROGRESS);
                    }
                }
            }
        }
        return clonedTemplate;
    }

    private void handleBuildPipelineTemplate(PipelineExecution pipelineExecution, PipelineExecution.StageExecution stageExecution) {
        log.info("Handling build pipeline execution for ID: {}", pipelineExecution.getId());

        log.info("Triggering build provider for stage: {}", stageExecution.getStageName());

        try {
            // 1. Fetch the Metadata (Build Profile) using the ID stored in your
            // request/execution context
            String metaDataId = (String) pipelineExecution.getMetaData().get("metaDataId");

            List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(
                    Map.of("_id", new ObjectId(metaDataId)));

            if (CollectionUtils.isEmpty(metadataList)) {
                log.error("Metadata not found for ID: {}", metaDataId);
                throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
            }
            Metadata buildProfile = metadataList.getFirst();

            // Extract providerID from build profile metadata
            String providerID = null;
            try {
                JsonNode data = objectMapper.readTree(Base64Util.convertToPlainText(buildProfile.getData()));
                providerID = data.path("buildProviderId").asText();
                if (StringUtils.hasText(providerID)) {
                    pipelineExecution.getMetaData().put("providerID", providerID);
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to extract providerID from build profile metadata for pipeline execution {}", pipelineExecution.getId(), e);
            }

            // 2. Extract values from the template steps (the 'values' map populated in
            // generateTemplateForPipeline)
            Map<String, String> values = new HashMap<>();
            stageExecution.getTemplate().getSteps().forEach(step -> {
                if (step.getValues() != null) {
                    step.getValues().forEach((k, v) -> values.put(k, String.valueOf(v)));
                }
            });

            // 3. Call the Build Provider
            BuildTriggerResponse buildTriggerResponse = buildProvider.triggerBuild(pipelineExecution,
                    buildProfile, values);

            // 4. Update Stage Status and store build URL in step metadata
            stageExecution.setStatus(PipelineStepExecutionStatus.IN_PROGRESS);
            if (pipelineExecution.getMetaData() == null) {
                pipelineExecution.setMetaData(new HashMap<>());
            }
            if (buildTriggerResponse != null && buildTriggerResponse.data() != null) {
                pipelineExecution.getMetaData().put("ciCaptainBuildId", buildTriggerResponse.data().buildId());
                pipelineExecution.getMetaData().put("ciCaptainStageExecutionId", stageExecution.getId());
                
                // Store build URL in the build step's metadata
                if (stageExecution.getTemplate() != null && !CollectionUtils.isEmpty(stageExecution.getTemplate().getSteps())) {
                    stageExecution.getTemplate().getSteps().forEach(step -> {
                        if ("build".equalsIgnoreCase(step.getStepName())) {
                            if (step.getMetadata() == null) {
                                step.setMetadata(new HashMap<>());
                            }
                            if (StringUtils.hasText(buildTriggerResponse.data().url())) {
                                step.getMetadata().put("buildUrl", buildTriggerResponse.data().url());
                            }
                        }
                    });
                }
            }
            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);

        } catch (Exception e) {
            log.error("Failed to trigger build for pipeline execution {}", pipelineExecution.getId(), e);
            stageExecution.setStatus(PipelineStepExecutionStatus.FAILED);
            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
        }
    }

    private void handleDeploymentPipelineTemplate(PipelineExecution pipelineExecution, PipelineExecution.StageExecution stageExecution) {
        log.info("Handling deployment pipeline execution for ID: {}", pipelineExecution.getId());

        try {
            // Update stage status to IN_PROGRESS
            stageExecution.setStatus(PipelineStepExecutionStatus.IN_PROGRESS);
            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);

            // Find the first pending step in the template
            Template.Step firstPendingStep = null;
            if (stageExecution.getTemplate() != null && stageExecution.getTemplate().getSteps() != null) {
                for (Template.Step step : stageExecution.getTemplate().getSteps()) {
                    if (step.getStatus() == StepExecutionStatus.IN_PROGRESS || step.getStatus() == StepExecutionStatus.CREATED) {
                        firstPendingStep = step;
                        break;
                    }
                }
            }

            if (firstPendingStep == null) {
                log.error("No pending step found for deployment stage in pipeline execution: {}", pipelineExecution.getId());
                stageExecution.setStatus(PipelineStepExecutionStatus.FAILED);
                writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
                return;
            }

            // Trigger the first step via cloudedge-deployer API
            DeplOrchClient deplOrchClient = applicationContext.getBean(DeplOrchClient.class);
            DeploymentResponse response = deplOrchClient.triggerPipelineDeployment(
                    pipelineExecution.getId(), 
                    stageExecution.getId(), 
                    firstPendingStep.getStepName()
            ).block();

            if (response != null) {
                log.info("Deployment trigger response for step {}: {}", firstPendingStep.getStepName(), response);
                // Step status will be updated via Kafka events from cloudedge-deployer
            } else {
                log.error("Failed to trigger deployment step {} for pipeline execution: {}", firstPendingStep.getStepName(), pipelineExecution.getId());
                firstPendingStep.setStatus(StepExecutionStatus.FAILED);
                writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
            }

        } catch (Exception e) {
            log.error("Failed to trigger deployment for pipeline execution {}", pipelineExecution.getId(), e);
            stageExecution.setStatus(PipelineStepExecutionStatus.FAILED);
            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
        }
    }

    public void processBuildCompletion(String buildRefId, String buildStatus) {
        log.info("Processing build completion for buildRefId: {} with status: {}", buildRefId, buildStatus);

        // Find pipeline execution by ciCaptainBuildId
        List<PipelineExecution> pipelineExecutions = readTransactionService.findByDynamicOrFilters(
                Map.of("metaData.ciCaptainBuildId", buildRefId), PipelineExecution.class);
        
        if (CollectionUtils.isEmpty(pipelineExecutions)) {
            log.info("No pipeline execution found for buildRefId: {}", buildRefId);
            return;
        }

        PipelineExecution pipelineExecution = pipelineExecutions.getFirst();
        log.info("Found pipeline execution: {} for buildRefId: {}", pipelineExecution.getId(), buildRefId);

        // Find the build stage and update its status
        pipelineExecution.getStageExecutions().sort((o1, o2) -> o1.getOrder() - o2.getOrder());
        
        PipelineExecution.StageExecution currentStage = null;
        for (PipelineExecution.StageExecution stageExecution : pipelineExecution.getStageExecutions()) {
            if (stageExecution.getStatus() == PipelineStepExecutionStatus.IN_PROGRESS) {
                currentStage = stageExecution;
                break;
            }
        }

        if (currentStage == null) {
            log.info("No in-progress stage found for pipeline execution: {}", pipelineExecution.getId());
            return;
        }

        // Update current stage status based on build status
        if ("SUCCESS".equalsIgnoreCase(buildStatus)) {
            currentStage.setStatus(PipelineStepExecutionStatus.SUCCESS);
            currentStage.setEndTime(LocalDateTime.now());
            log.info("Stage {} completed successfully for pipeline execution: {}", currentStage.getStageName(), pipelineExecution.getId());
        } else {
            currentStage.setStatus(PipelineStepExecutionStatus.FAILED);
            currentStage.setEndTime(LocalDateTime.now());
            log.info("Stage {} failed for pipeline execution: {}", currentStage.getStageName(), pipelineExecution.getId());
            
            // Check if we should stop on failure
            if (currentStage.isStopOnFailure()) {
                pipelineExecution.setStatus(PipelineStatus.FAILED);
                writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
                log.info("Pipeline execution {} stopped due to stage failure with stopOnFailure=true", pipelineExecution.getId());
                return;
            }
        }

        writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);

        // Trigger next stage if current stage succeeded
        if ("SUCCESS".equalsIgnoreCase(buildStatus)) {
            triggerNextStage(pipelineExecution);
        }
    }

    @Override
    public void triggerNextStage(PipelineExecution pipelineExecution) {
        pipelineExecution.getStageExecutions().sort((o1, o2) -> o1.getOrder() - o2.getOrder());
        
        // Find the next pending stage
        for (PipelineExecution.StageExecution stageExecution : pipelineExecution.getStageExecutions()) {
            if (stageExecution.getStatus() == PipelineStepExecutionStatus.PENDING) {
                log.info("Triggering next stage: {} for pipeline execution: {}", stageExecution.getStageName(), pipelineExecution.getId());
                
                switch (stageExecution.getStageName().toLowerCase()) {
                    case "build":
                        handleBuildPipelineTemplate(pipelineExecution, stageExecution);
                        break;
                    case "deployment":
                        handleDeploymentPipelineTemplate(pipelineExecution, stageExecution);
                        break;
                    case "completed":
                        // Automatically complete the completed stage
                        stageExecution.setStatus(PipelineStepExecutionStatus.SUCCESS);
                        stageExecution.setEndTime(LocalDateTime.now());
                        writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
                        log.info("Automatically completed stage {} for pipeline execution: {}", stageExecution.getStageName(), pipelineExecution.getId());
                        // Trigger next stage (which should complete the pipeline)
                        triggerNextStage(pipelineExecution);
                        return;
                    default:
                        log.error("Unknown pipeline template type for pipeline execution {}", pipelineExecution.getId());
                }
                return;
            }
        }
        
        // All stages completed
        pipelineExecution.setStatus(PipelineStatus.SUCCESS);
        writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
        log.info("Pipeline execution {} completed successfully", pipelineExecution.getId());
    }

    private PipelineTemplate fetchPipelineTemplate(Map<String, Object> filters) {
        List<PipelineTemplate> pipelineTemplates = readTransactionService.findByDynamicOrFilters(filters,
                PipelineTemplate.class);
        if (CollectionUtils.isEmpty(pipelineTemplates)) {
            log.error("Pipeline template could not be found with filters {}", filters);
            throw new CustomException(ErrorCode.PIPELINE_TEMPLATE_COULD_NOT_BE_FOUND);
        }
        return pipelineTemplates.getFirst();
    }

    private void handleParamGenerationForBuildStep(PipelineExecutionRequest request, Template.Step step,
            JsonNode profileConfig, Metadata buildProfile) {

        String decodedData = Base64Util.convertToPlainText(buildProfile.getData());
        String commitId = String.valueOf(request.getMetaData().get("commitId"));
        String dockerImageHashValue = buildProfile.getApplication().getName().toLowerCase() + "-"
                + Base64Util.generate7DigitHash(decodedData) + ":" + commitId.substring(0, 7);

        String scmProviderId = profileConfig.path("scmId").asText();

        Map<String, Object> configs = new HashMap<>();
        for (Map.Entry<String, Template.Step.TemplateStepParam> itr : step.getParams().entrySet()) {
            if (ObjectUtils.isEmpty(profileConfig.path(itr.getKey()))) {
                configs.put(itr.getKey(), "");
            } else {
                switch (itr.getKey()) {
                    case "commitId":
                        itr.getValue().setValue(commitId);
                        break;
                    case "repoURL":
                        itr.getValue().setValue(credentialProviderService.extractSCMURL(scmProviderId) + "/"
                                + profileConfig.path("repo").asText());
                        break;
                    case "serviceName":
                        itr.getValue().setValue(buildProfile.getApplication().getName());
                        break;
                    case "dockerImageHashValue":
                        itr.getValue().setValue(dockerImageHashValue);
                        break;
                    default:
                        itr.getValue().setValue(profileConfig.path(itr.getKey()).asText());
                        configs.put(itr.getKey(), profileConfig.path(itr.getKey()).asText());
                }
            }
        }
        step.setValues(configs);
        step.setMetadata(new HashMap<>());
        step.setStatus(StepExecutionStatus.IN_PROGRESS);
    }

    @Override
    public PipelineExecutionDetailsDTO getPipelineExecutionDetails(String pipelineExecutionId) {
        List<PipelineExecution> pipelineExecutions = readTransactionService
                .findByDynamicOrFilters(Map.of("_id", new ObjectId(pipelineExecutionId)), PipelineExecution.class);
        if (CollectionUtils.isEmpty(pipelineExecutions)) {
            log.error("Pipeline execution could not be found with ID {}", pipelineExecutionId);
            throw new CustomException(ErrorCode.PIPELINE_EXECUTION_COULD_NOT_BE_FOUND);
        }
        PipelineExecution pipelineExecution = pipelineExecutions.getFirst();

        return mapToDetailsDTO(pipelineExecution);
    }

    @Override
    public List<PipelineExecutionDetailsDTO> getPipelineExecutions(Map<String, Object> filters) {
        Map<String, Object> queryFilters = new HashMap<>();

        filters.forEach((key, value) -> {
            if (key.equalsIgnoreCase("serviceId")) {
                queryFilters.put("metaData.serviceId", value);
            } else {
                queryFilters.put(key, value);
            }
        });

        List<PipelineExecution> pipelineExecutions = readTransactionService.findPipelineExecutionsByFilters(queryFilters);

        return pipelineExecutions.stream()
                .map(this::mapToDetailsDTO)
                .toList();
    }

    @Override
    public PaginatedResponse<PipelineExecutionDetailsDTO> getPipelineExecutionsPaginated(
            Map<String, Object> filters,
            Integer page,
            Integer size,
            String sortBy,
            String order) {
        Map<String, Object> queryFilters = new HashMap<>();

        filters.forEach((key, value) -> {
            if (key.equalsIgnoreCase("serviceId")) {
                queryFilters.put("metaData.serviceId", value);
            } else {
                queryFilters.put(key, value);
            }
        });

        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<PipelineExecution> pipelineExecutionsPage = readTransactionService.findPipelineExecutionsByFiltersPaginated(
                queryFilters, page, size, sortBy, direction);

        List<PipelineExecutionDetailsDTO> content = pipelineExecutionsPage.getContent().stream()
                .map(this::mapToDetailsDTO)
                .toList();

        return PaginatedResponse.<PipelineExecutionDetailsDTO>builder()
                .content(content)
                .pageNumber(pipelineExecutionsPage.getNumber())
                .pageSize(pipelineExecutionsPage.getSize())
                .totalElements(pipelineExecutionsPage.getTotalElements())
                .totalPages(pipelineExecutionsPage.getTotalPages())
                .isLast(pipelineExecutionsPage.isLast())
                .build();
    }

    @Override
    public void syncPipelineStatus(String pipelineExecutionId, String forceSync) {
        log.info("Syncing pipeline status for execution {} with forceSync={}", pipelineExecutionId, forceSync);
        
        List<PipelineExecution> pipelineExecutions = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", new ObjectId(pipelineExecutionId)),
                PipelineExecution.class
        );
        
        if (CollectionUtils.isEmpty(pipelineExecutions)) {
            log.error("Pipeline execution not found with ID {}", pipelineExecutionId);
            throw new CustomException(ErrorCode.PIPELINE_EXECUTION_COULD_NOT_BE_FOUND);
        }
        
        PipelineExecution pipelineExecution = pipelineExecutions.getFirst();
        
        // Get the build stage execution
        PipelineExecution.StageExecution buildStage = pipelineExecution.getStageExecutions().stream()
                .filter(stage -> "build".equalsIgnoreCase(stage.getStageName()))
                .findFirst()
                .orElse(null);
                
        if (buildStage == null) {
            log.warn("Build stage not found for pipeline execution {}", pipelineExecutionId);
            return;
        }
        
        // Get buildRefId from metadata
        String buildRefId = (String) pipelineExecution.getMetaData().get("ciCaptainBuildId");
        if (!StringUtils.hasText(buildRefId)) {
            log.warn("BuildRefId not found in metadata for pipeline execution {}", pipelineExecutionId);
            return;
        }
        
        // Get providerID from metadata or build profile
        String providerID = (String) pipelineExecution.getMetaData().get("providerID");
        if (!StringUtils.hasText(providerID)) {
            // Try to get providerID from build profile
            String metaDataId = (String) pipelineExecution.getMetaData().get("metaDataId");
            if (StringUtils.hasText(metaDataId)) {
                List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(
                        Map.of("_id", new ObjectId(metaDataId)));
                if (!CollectionUtils.isEmpty(metadataList)) {
                    Metadata buildProfile = metadataList.getFirst();
                    try {
                        JsonNode data = objectMapper.readTree(Base64Util.convertToPlainText(buildProfile.getData()));
                        providerID = data.path("buildProviderId").asText();
                        if (StringUtils.hasText(providerID)) {
                            // Store it in metadata for future use
                            pipelineExecution.getMetaData().put("providerID", providerID);
                            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
                        }
                    } catch (JsonProcessingException e) {
                        log.warn("Failed to extract providerID from build profile metadata for pipeline execution {}", pipelineExecutionId, e);
                    }
                }
            }
        }
        
        if (!StringUtils.hasText(providerID)) {
            log.warn("ProviderID not found in metadata or build profile for pipeline execution {}", pipelineExecutionId);
            return;
        }
        
        try {
            // Fetch build status from CI-Captain
            reactor.core.publisher.Mono<com.swadeshitech.prodhub.integration.cicaptain.dto.BuildStatusResponse> buildStatusResponseMono = 
                ciCaptainClient.getBuildStatus(providerID, buildRefId, forceSync);
            com.swadeshitech.prodhub.integration.cicaptain.dto.BuildStatusResponse response = buildStatusResponseMono.blockOptional().get();
            
            log.info("Build status from CI-Captain: {}", response.status());
            
            // Update the build stage status based on CI-Captain response
            if ("SUCCESS".equalsIgnoreCase(response.status())) {
                buildStage.setStatus(PipelineStepExecutionStatus.SUCCESS);
                buildStage.setEndTime(LocalDateTime.now());
                
                // Update build step status
                if (buildStage.getTemplate() != null && !CollectionUtils.isEmpty(buildStage.getTemplate().getSteps())) {
                    buildStage.getTemplate().getSteps().forEach(step -> {
                        if ("build".equalsIgnoreCase(step.getStepName())) {
                            step.setStatus(StepExecutionStatus.COMPLETED);
                        }
                    });
                }
                
                // Check if all stages are completed
                boolean allStagesCompleted = pipelineExecution.getStageExecutions().stream()
                        .sorted((o1, o2) -> o1.getOrder() - o2.getOrder())
                        .filter(stage -> !"init".equalsIgnoreCase(stage.getStageName()))
                        .allMatch(stage -> stage.getStatus() == PipelineStepExecutionStatus.SUCCESS);
                        
                pipelineExecution.setStatus(allStagesCompleted ? PipelineStatus.SUCCESS : PipelineStatus.IN_PROGRESS);
                
                // Trigger next stage if not all stages are completed
                if (!allStagesCompleted) {
                    log.info("Triggering next stage after status sync for pipeline execution {}", pipelineExecutionId);
                    triggerNextStage(pipelineExecution);
                }
            } else if ("FAILURE".equalsIgnoreCase(response.status()) || "FAILED".equalsIgnoreCase(response.status())) {
                buildStage.setStatus(PipelineStepExecutionStatus.FAILED);
                buildStage.setEndTime(LocalDateTime.now());
                pipelineExecution.setStatus(PipelineStatus.FAILED);
            }
            
            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
            log.info("Successfully synced pipeline status for execution {}", pipelineExecutionId);
            
        } catch (Exception e) {
            log.error("Failed to sync pipeline status for execution {}", pipelineExecutionId, e);
            throw new CustomException(ErrorCode.PIPELINE_EXECUTION_COULD_NOT_BE_FOUND);
        }
    }

    private PipelineExecutionDetailsDTO mapToDetailsDTO(PipelineExecution pipelineExecution) {
        List<StageExecutionDTO> stageExecutions = pipelineExecution.getStageExecutions() != null
                ? pipelineExecution.getStageExecutions().stream()
                        .sorted((o1, o2) -> o1.getOrder() - o2.getOrder())
                        .map(stageExecution -> StageExecutionDTO.builder()
                                .id(stageExecution.getId())
                                .stageName(stageExecution.getStageName())
                                .template(stageExecution.getTemplate() != null
                                        ? TemplateResponse.mapDTOToEntity(stageExecution.getTemplate())
                                        : null)
                                .status(stageExecution.getStatus())
                                .order(stageExecution.getOrder())
                                .stopOnFailure(stageExecution.isStopOnFailure())
                                .startTime(stageExecution.getStartTime())
                                .endTime(stageExecution.getEndTime())
                                .build())
                        .toList()
                : null;

        // Fetch release candidate ID associated with this pipeline execution
        String releaseCandidateId = null;
        try {
            List<com.swadeshitech.prodhub.entity.ReleaseCandidate> releaseCandidates = 
                readTransactionService.findByDynamicOrFilters(
                    Map.of("pipelineExecution.$id", new ObjectId(pipelineExecution.getId())),
                    com.swadeshitech.prodhub.entity.ReleaseCandidate.class
                );
            if (!CollectionUtils.isEmpty(releaseCandidates)) {
                releaseCandidateId = releaseCandidates.getFirst().getId();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch release candidate for pipeline execution {}", pipelineExecution.getId(), e);
        }

        return PipelineExecutionDetailsDTO.builder()
                .id(pipelineExecution.getId())
                .status(pipelineExecution.getStatus())
                .metaData(pipelineExecution.getMetaData())
                .stageExecutions(stageExecutions)
                .createdBy(pipelineExecution.getCreatedBy())
                .createdTime(pipelineExecution.getCreatedTime())
                .releaseCandidateId(releaseCandidateId)
                .build();
    }
}
