package com.swadeshitech.prodhub.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.dto.PipelineExecutionRequest;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.entity.PipelineTemplate;
import com.swadeshitech.prodhub.entity.Template;
import com.swadeshitech.prodhub.enums.*;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerResponse;
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

    @Override
    public String schedulePipelineExecution(PipelineExecutionRequest request) {

        PipelineExecution pipelineExecution = createPipelineExecution(request);

        kafkaProducer.sendMessage(pipelineExecutionTopicName, pipelineExecution.getId());

        return pipelineExecution.getId();
    }

    @Override
    public PipelineExecution createPipelineExecution(PipelineExecutionRequest request) {

        PipelineTemplate pipelineTemplate = fetchPipelineTemplate(
                Map.of("name", request.getPipelineTemplateName())
        );

        PipelineExecution pipelineExecution = new PipelineExecution();
        pipelineExecution.setStageExecutions(createStages(request, pipelineTemplate, request.getMetaDataID()));
        pipelineExecution.setPipelineTemplate(pipelineTemplate);
        pipelineExecution.setStatus(PipelineStatus.PENDING);
        pipelineExecution.setMetaData(new HashMap<>(Map.of("metaDataId", request.getMetaDataID())));

        return writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
    }

    @Override
    public void startPipelineExecution(PipelineExecution pipelineExecution) {
        pipelineExecution.getStageExecutions().sort((o1, o2) -> o1.getOrder() - o2.getOrder());
        for(PipelineExecution.StageExecution stageExecution : pipelineExecution.getStageExecutions()) {
            if(Objects.nonNull(stageExecution)) {
                switch (stageExecution.getStageName().toLowerCase()) {
                    case "build":
                        handleBuildPipelineTemplate(pipelineExecution);
                        break;
                    case "deployment":
                        handleDeploymentPipelineTemplate(pipelineExecution);
                        break;
                    default:
                        log.error("Unknow pipeline template type for pipeline execution {}", pipelineExecution.getId());
                }
            }
        }
    }

    private List<PipelineExecution.StageExecution> createStages(PipelineExecutionRequest request, PipelineTemplate pipelineTemplate, String metaDataId) {
        List<PipelineExecution.StageExecution> stages = new ArrayList<>();

        for(PipelineTemplate.StageDefinition stageDefinition : pipelineTemplate.getStages()) {
            PipelineExecution.StageExecution stageExecution = PipelineExecution.StageExecution.builder()
                    .id(UuidUtil.generateRandomUuid())
                    .stageName(stageDefinition.getName())
                    .startTime(LocalDateTime.now())
                    .stopOnFailure(stageDefinition.isStopOnFailure())
                    .order(stageDefinition.getOrder())
                    .status(PipelineStepExecutionStatus.PENDING)
                    .template(generateTemplateForPipeline(request, stageDefinition.getTemplateName(), metaDataId))
                    .build();
            if(stageDefinition.getName().equalsIgnoreCase("init")) {
                stageExecution.setStatus(PipelineStepExecutionStatus.SUCCESS);
            }
            stages.add(stageExecution);
        }

        return stages;
    }

    private Template generateTemplateForPipeline(PipelineExecutionRequest request, String templateName, String metaDataId) {

        if(!StringUtils.hasText(templateName)) {
            return null;
        }

        List<Template> templates = readTransactionService.findByDynamicOrFilters(
                Map.of("templateName", templateName),
                Template.class
        );
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
            if(step.getStepName().equalsIgnoreCase("init")) {
                step.setStatus(StepExecutionStatus.COMPLETED);
            } else {
                if (!CollectionUtils.isEmpty(step.getParams()) && !step.isSkipStep()) {
                    if(step.getStepName().equalsIgnoreCase("build")) {
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

    private void handleBuildPipelineTemplate(PipelineExecution pipelineExecution) {
        log.info("Handling build pipeline execution for ID: {}", pipelineExecution.getId());

        for (PipelineExecution.StageExecution stageExecution : pipelineExecution.getStageExecutions()) {
            // Check if the stage is intended for building (case-insensitive check based on your JSON)
            if (stageExecution.getStageName().equalsIgnoreCase("Build")) {

                log.info("Triggering build provider for stage: {}", stageExecution.getStageName());

                try {
                    // 1. Fetch the Metadata (Build Profile) using the ID stored in your request/execution context
                    // Note: You may need to ensure the metadata ID is passed correctly.
                    // Assuming the metadata ID is part of the execution's metaData map or reachable via the template name
                    String metaDataId = (String) pipelineExecution.getMetaData().get("metaDataId");

                    List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(
                            Map.of("_id", new ObjectId(metaDataId)));

                    if (CollectionUtils.isEmpty(metadataList)) {
                        log.error("Metadata not found for ID: {}", metaDataId);
                        throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
                    }
                    Metadata buildProfile = metadataList.getFirst();

                    // 2. Extract values from the template steps (the 'values' map populated in generateTemplateForPipeline)
                    Map<String, String> values = new HashMap<>();
                    stageExecution.getTemplate().getSteps().forEach(step -> {
                        if (step.getValues() != null) {
                            step.getValues().forEach((k, v) -> values.put(k, String.valueOf(v)));
                        }
                    });

                    // 3. Call the Build Provider
                    BuildTriggerResponse buildTriggerResponse = buildProvider.triggerBuild(pipelineExecution, buildProfile, values);

                    // 4. Update Stage Status
                    stageExecution.setStatus(PipelineStepExecutionStatus.IN_PROGRESS);
                    if (pipelineExecution.getMetaData() == null) {
                        pipelineExecution.setMetaData(new HashMap<>());
                    }
                    if (buildTriggerResponse != null && buildTriggerResponse.data() != null) {
                        pipelineExecution.getMetaData().put("ciCaptainBuildId", buildTriggerResponse.data().buildId());
                        pipelineExecution.getMetaData().put("ciCaptainStageExecutionId", stageExecution.getId());
                    }
                    writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);

                } catch (Exception e) {
                    log.error("Failed to trigger build for pipeline execution {}", pipelineExecution.getId(), e);
                    stageExecution.setStatus(PipelineStepExecutionStatus.FAILED);
                    writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
                }
            }
        }
    }

    private void handleDeploymentPipelineTemplate(PipelineExecution pipelineExecution) {

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

    private void handleParamGenerationForBuildStep(PipelineExecutionRequest request, Template.Step step, JsonNode profileConfig, Metadata buildProfile) {

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
                    case "commitId": itr.getValue().setValue(commitId);
                    break;
                    case "repoURL": itr.getValue().setValue(credentialProviderService.extractSCMURL(scmProviderId) + "/" + profileConfig.path("repo").asText());
                    break;
                    case "serviceName": itr.getValue().setValue(buildProfile.getApplication().getName());
                    break;
                    case "dockerImageHashValue": itr.getValue().setValue(dockerImageHashValue);
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
}
