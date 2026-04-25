package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.dto.DeploymentUpdateKafka;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.entity.Template;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.PipelineStepExecutionStatus;
import com.swadeshitech.prodhub.enums.StepExecutionStatus;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.DeploymentService;
import com.swadeshitech.prodhub.services.PipelineService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DeploymentUpdateConsumer {

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    PipelineService pipelineService;

    @Autowired
    org.springframework.context.ApplicationContext applicationContext;

    @KafkaListener(topics = "${spring.kafka.topic.deploymentUpdates}", groupId = "default_group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message) {

        log.info("{}: Request for deployment update {}", this.getClass().getCanonicalName(), message);

        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String deploymentRequestId = jsonNode.path("deploymentRequestId").asText();
            String stepName = jsonNode.path("stepName").asText();
            String status = jsonNode.path("status").asText();
            String timestamp = jsonNode.path("timestamp").asText();
            String details = jsonNode.path("details").asText();

            // Check if this is a pipeline execution ID (cloudedge-deployer now uses pipeline execution IDs)
            List<PipelineExecution> pipelineExecutions = readTransactionService.findByDynamicOrFilters(
                    Map.of("_id", new ObjectId(deploymentRequestId)), PipelineExecution.class);

            if (!CollectionUtils.isEmpty(pipelineExecutions)) {
                // This is a pipeline execution ID - handle as pipeline execution update
                log.info("Treating deployment update as pipeline execution update for ID: {}", deploymentRequestId);
                handlePipelineExecutionUpdate(deploymentRequestId, stepName, status, details);
            } else {
                // This is a deployment ID - handle as deployment update (legacy)
                log.info("Treating as legacy deployment update for ID: {}", deploymentRequestId);
                DeploymentUpdateKafka deploymentUpdateKafka = objectMapper.readValue(message, DeploymentUpdateKafka.class);
                deploymentService.updateDeploymentStepStatus(deploymentUpdateKafka);
            }
        } catch (JsonProcessingException e) {
            log.error("{}: Fail to parse the kafka message", this.getClass().getCanonicalName(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void handlePipelineExecutionUpdate(String pipelineExecutionId, String stepName, String status, String statusMessage) {
        try {
            // Find pipeline execution by ID
            List<PipelineExecution> pipelineExecutions = readTransactionService.findByDynamicOrFilters(
                    Map.of("_id", new ObjectId(pipelineExecutionId)), PipelineExecution.class);

            if (CollectionUtils.isEmpty(pipelineExecutions)) {
                log.error("Pipeline execution not found for ID: {}", pipelineExecutionId);
                return;
            }

            PipelineExecution pipelineExecution = pipelineExecutions.getFirst();

            // Find the stage execution that contains this step
            PipelineExecution.StageExecution targetStage = null;
            String targetStageExecutionId = null;

            for (PipelineExecution.StageExecution stageExecution : pipelineExecution.getStageExecutions()) {
                if (stageExecution.getTemplate() != null && stageExecution.getTemplate().getSteps() != null) {
                    for (Template.Step step : stageExecution.getTemplate().getSteps()) {
                        if (step.getStepName().equalsIgnoreCase(stepName)) {
                            targetStage = stageExecution;
                            targetStageExecutionId = stageExecution.getId();
                            break;
                        }
                    }
                }
                if (targetStage != null) {
                    break;
                }
            }

            if (targetStage == null) {
                log.error("Stage execution not found for step {} in pipeline execution: {}", stepName, pipelineExecutionId);
                return;
            }

            // Update step status in template
            if (targetStage.getTemplate() != null && targetStage.getTemplate().getSteps() != null) {
                for (Template.Step step : targetStage.getTemplate().getSteps()) {
                    if (step.getStepName().equalsIgnoreCase(stepName)) {
                        if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                            step.setStatus(StepExecutionStatus.IN_PROGRESS);
                        } else if ("COMPLETED".equalsIgnoreCase(status)) {
                            step.setStatus(StepExecutionStatus.COMPLETED);
                        } else if ("FAILED".equalsIgnoreCase(status)) {
                            step.setStatus(StepExecutionStatus.FAILED);
                        }
                        break;
                    }
                }
            }

            // Update stage status based on received status
            if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                targetStage.setStatus(PipelineStepExecutionStatus.IN_PROGRESS);
                if (targetStage.getStartTime() == null) {
                    targetStage.setStartTime(LocalDateTime.now());
                }
            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                // Individual step completed - trigger next step in the same stage
                log.info("Step {} completed for stage {} in pipeline execution: {}", stepName, targetStage.getStageName(), pipelineExecutionId);
                triggerNextStepInStage(pipelineExecution, targetStage);
            } else if ("FAILED".equalsIgnoreCase(status)) {
                targetStage.setStatus(PipelineStepExecutionStatus.FAILED);
                targetStage.setEndTime(LocalDateTime.now());
                log.info("Stage {} failed for pipeline execution: {}. Reason: {}", targetStage.getStageName(), pipelineExecutionId, statusMessage);

                // Check if we should stop on failure
                if (targetStage.isStopOnFailure()) {
                    pipelineExecution.setStatus(com.swadeshitech.prodhub.enums.PipelineStatus.FAILED);
                    writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
                    log.info("Pipeline execution {} stopped due to stage failure with stopOnFailure=true", pipelineExecutionId);
                    return;
                }

                // Trigger next stage if not stopping on failure
                pipelineService.triggerNextStage(pipelineExecution);
            }

            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
            log.info("Pipeline execution {} updated successfully via deployment-update topic", pipelineExecutionId);

        } catch (Exception e) {
            log.error("Error processing pipeline execution update from deployment-update topic for ID: {}", pipelineExecutionId, e);
        }
    }

    private void triggerNextStepInStage(PipelineExecution pipelineExecution, PipelineExecution.StageExecution stageExecution) {
        if (stageExecution.getTemplate() == null || stageExecution.getTemplate().getSteps() == null) {
            log.info("No steps found in template for stage: {}", stageExecution.getStageName());
            // All steps completed - mark stage as success and trigger next stage
            stageExecution.setStatus(PipelineStepExecutionStatus.SUCCESS);
            stageExecution.setEndTime(LocalDateTime.now());
            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
            pipelineService.triggerNextStage(pipelineExecution);
            return;
        }

        // Find the next pending step
        Template.Step nextStep = null;
        boolean hasPendingSteps = false;
        boolean hasFailedSteps = false;

        for (Template.Step step : stageExecution.getTemplate().getSteps()) {
            if (step.getStatus() == StepExecutionStatus.CREATED || step.getStatus() == StepExecutionStatus.IN_PROGRESS) {
                nextStep = step;
                hasPendingSteps = true;
                break;
            } else if (step.getStatus() == StepExecutionStatus.FAILED) {
                hasFailedSteps = true;
            }
        }

        if (hasFailedSteps) {
            // A step failed - mark stage as failed
            stageExecution.setStatus(PipelineStepExecutionStatus.FAILED);
            stageExecution.setEndTime(LocalDateTime.now());
            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);

            if (stageExecution.isStopOnFailure()) {
                pipelineExecution.setStatus(com.swadeshitech.prodhub.enums.PipelineStatus.FAILED);
                writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
                log.info("Pipeline execution {} stopped due to step failure with stopOnFailure=true", pipelineExecution.getId());
                return;
            }

            pipelineService.triggerNextStage(pipelineExecution);
            return;
        }

        if (nextStep == null) {
            // All steps completed - mark stage as success and trigger next stage
            stageExecution.setStatus(PipelineStepExecutionStatus.SUCCESS);
            stageExecution.setEndTime(LocalDateTime.now());
            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
            log.info("All steps completed for stage {} in pipeline execution: {}", stageExecution.getStageName(), pipelineExecution.getId());
            pipelineService.triggerNextStage(pipelineExecution);
            return;
        }

        // Trigger the next step via cloudedge-deployer API
        try {
            com.swadeshitech.prodhub.integration.deplorch.DeplOrchClient deplOrchClient = applicationContext.getBean(com.swadeshitech.prodhub.integration.deplorch.DeplOrchClient.class);
            com.swadeshitech.prodhub.integration.deplorch.DeploymentResponse response = deplOrchClient.triggerPipelineDeployment(
                    pipelineExecution.getId(),
                    stageExecution.getId(),
                    nextStep.getStepName()
            ).block();

            if (response != null) {
                log.info("Triggered next step {} for stage {} in pipeline execution: {}", nextStep.getStepName(), stageExecution.getStageName(), pipelineExecution.getId());
            } else {
                log.error("Failed to trigger step {} for pipeline execution: {}", nextStep.getStepName(), pipelineExecution.getId());
                nextStep.setStatus(StepExecutionStatus.FAILED);
                writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
            }
        } catch (Exception e) {
            log.error("Error triggering next step {} for pipeline execution: {}", nextStep.getStepName(), pipelineExecution.getId(), e);
            nextStep.setStatus(StepExecutionStatus.FAILED);
            writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
        }
    }
}
