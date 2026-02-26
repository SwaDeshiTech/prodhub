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
import com.swadeshitech.prodhub.integration.kafka.producer.KafkaProducer;
import com.swadeshitech.prodhub.provider.BuildProvider;
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

    @Override
    public String schedulePipelineExecution(PipelineExecutionRequest request) {

        PipelineExecution pipelineExecution = createPipelineExecution(request.getPipelineTemplateName(),
                request.getMetaDataID());

        kafkaProducer.sendMessage(pipelineExecutionTopicName, pipelineExecution.getId());

        return pipelineExecution.getId();
    }

    @Override
    public PipelineExecution createPipelineExecution(String pipelineTemplateName, String metaDataId) {

        PipelineTemplate pipelineTemplate = fetchPipelineTemplate(
                Map.of("name", pipelineTemplateName)
        );

        PipelineExecution pipelineExecution = new PipelineExecution();
        pipelineExecution.setStageExecutions(createStages(pipelineTemplate, metaDataId));
        pipelineExecution.setPipelineTemplate(pipelineTemplate);
        pipelineExecution.setStatus(PipelineStatus.PENDING);
        pipelineExecution.setMetaData(Map.of());

        return writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
    }

    @Override
    public void startPipelineExecution(PipelineExecution pipelineExecution) {
        if(Objects.nonNull(pipelineExecution)) {
            switch (pipelineExecution.getPipelineTemplate().getPipelineTemplateType()) {
                case PipelineTemplateType.BUILD:
                    handleBuildPipelineTemplate(pipelineExecution);
                    break;
                case PipelineTemplateType.DEPLOYMENT:
                    handleDeploymentPipelineTemplate(pipelineExecution);
                    break;
                default:
                    log.error("Unknow pipeline template type for pipeline execution {}", pipelineExecution.getId());
            }
        }
    }

    private List<PipelineExecution.StageExecution> createStages(PipelineTemplate pipelineTemplate, String metaDataId) {
        List<PipelineExecution.StageExecution> stages = new ArrayList<>();

        for(PipelineTemplate.StageDefinition stageDefinition : pipelineTemplate.getStages()) {
            stages.add(PipelineExecution.StageExecution.builder()
                    .id(UuidUtil.generateRandomUuid())
                    .stageName(stageDefinition.getName())
                    .startTime(LocalDateTime.now())
                    .stopOnFailure(stageDefinition.isStopOnFailure())
                    .order(stageDefinition.getOrder())
                    .status(PipelineStepExecutionStatus.PENDING)
                    .template(generateTemplateForPipeline(stageDefinition.getTemplateName(), metaDataId))
                    .build());
        }

        return stages;
    }

    private Template generateTemplateForPipeline(String templateName, String metaDataId) {

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

        for (Template.Step deploymentStep : clonedTemplate.getSteps()) {
            if (!CollectionUtils.isEmpty(deploymentStep.getParams()) && !deploymentStep.isSkipStep()) {
                Map<String, Object> configs = new HashMap<>();
                for (Map.Entry<String, Template.Step.TemplateStepParam> itr : deploymentStep.getParams().entrySet()) {
                    if (ObjectUtils.isEmpty(profileConfig.path(itr.getKey()))) {
                        configs.put(itr.getKey(), "");
                    } else {
                        configs.put(itr.getKey(), profileConfig.path(itr.getKey()).asText());
                    }
                }
                deploymentStep.setValues(configs);
            }
            deploymentStep.setMetadata(new HashMap<>());
            deploymentStep.setStatus(StepExecutionStatus.IN_PROGRESS);
        }

        return clonedTemplate;
    }

    private void handleBuildPipelineTemplate(PipelineExecution pipelineExecution) {
        for(PipelineExecution.StageExecution stageExecution : pipelineExecution.getStageExecutions()) {
            if(stageExecution.getTemplate().getTemplateName().equalsIgnoreCase("build")) {
                log.info("Triggering build for pipeline {}", stageExecution.getId());
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
}
