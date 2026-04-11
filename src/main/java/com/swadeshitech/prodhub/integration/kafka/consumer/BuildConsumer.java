package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.entity.Template;
import com.swadeshitech.prodhub.enums.PipelineStatus;
import com.swadeshitech.prodhub.enums.PipelineStepExecutionStatus;
import com.swadeshitech.prodhub.enums.StepExecutionStatus;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BuildConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @KafkaListener(topics = "${spring.kafka.topic.buildUpdate}", groupId = "default_group")
    public void listen(String message) {
        log.info("Message received for build update {}", message);
        BuildUpdateMessage buildUpdateMessage;
        try {
            buildUpdateMessage = objectMapper.readValue(message, BuildUpdateMessage.class);
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse build update payload {}", message, ex);
            return;
        }

        if (!StringUtils.hasText(buildUpdateMessage.buildId())) {
            log.warn("Ignoring build update without build_id");
            return;
        }

        String pipelineExecutionId = resolvePipelineExecutionId(buildUpdateMessage);
        if (!StringUtils.hasText(pipelineExecutionId)) {
            log.info("No pipeline execution mapping found for build {}", buildUpdateMessage.buildId());
            return;
        }

        List<PipelineExecution> pipelineExecutions = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", new ObjectId(pipelineExecutionId)),
                PipelineExecution.class
        );
        if (CollectionUtils.isEmpty(pipelineExecutions)) {
            log.warn("Pipeline execution {} not found for build {}", pipelineExecutionId, buildUpdateMessage.buildId());
            return;
        }

        PipelineExecution pipelineExecution = pipelineExecutions.getFirst();
        updateExecutionAndBuildStep(pipelineExecution, buildUpdateMessage);
        writeTransactionService.savePipelineExecutionToRepository(pipelineExecution);
    }

    private String resolvePipelineExecutionId(BuildUpdateMessage buildUpdateMessage) {
        if (buildUpdateMessage.data() != null && StringUtils.hasText(buildUpdateMessage.data().pipelineExecutionId())) {
            return buildUpdateMessage.data().pipelineExecutionId();
        }

        List<PipelineExecution> pipelineExecutions = readTransactionService.findByDynamicOrFilters(
                Map.of("metaData.ciCaptainBuildId", buildUpdateMessage.buildId()),
                PipelineExecution.class
        );
        if (CollectionUtils.isEmpty(pipelineExecutions)) {
            return null;
        }
        return pipelineExecutions.getFirst().getId();
    }

    private void updateExecutionAndBuildStep(PipelineExecution pipelineExecution, BuildUpdateMessage buildUpdateMessage) {
        PipelineExecution.StageExecution stageExecution = findBuildStageExecution(
                pipelineExecution,
                buildUpdateMessage.data() != null ? buildUpdateMessage.data().stageExecutionId() : null
        );
        if (stageExecution == null) {
            log.warn("Build stage not found in pipeline execution {}", pipelineExecution.getId());
            return;
        }

        StepExecutionStatus stepStatus = toStepStatus(buildUpdateMessage.status());
        updateStepStatus(stageExecution, "build", stepStatus);

        handleStepCompletion(pipelineExecution, stageExecution, stepStatus);
    }

    private void updateStepStatus(PipelineExecution.StageExecution stageExecution, String stepName, StepExecutionStatus status) {
        if (stageExecution.getTemplate() != null && !CollectionUtils.isEmpty(stageExecution.getTemplate().getSteps())) {
            stageExecution.getTemplate().getSteps().forEach(step -> {
                if (stepName.equalsIgnoreCase(step.getStepName())) {
                    step.setStatus(status);
                }
            });
        }
    }

    private void handleStepCompletion(PipelineExecution pipelineExecution, PipelineExecution.StageExecution currentStage, StepExecutionStatus stepStatus) {
        if (stepStatus == StepExecutionStatus.FAILED) {
            handleStepFailure(pipelineExecution, currentStage);
        } else if (stepStatus == StepExecutionStatus.COMPLETED) {
            handleStepSuccess(pipelineExecution, currentStage);
        }
    }

    private void handleStepFailure(PipelineExecution pipelineExecution, PipelineExecution.StageExecution failedStage) {
        log.info("Handling step failure for stage {} in pipeline {}", failedStage.getStageName(), pipelineExecution.getId());

        failedStage.setStatus(PipelineStepExecutionStatus.FAILED);
        failedStage.setEndTime(LocalDateTime.now());

        if (failedStage.isStopOnFailure()) {
            skipRemainingStages(pipelineExecution, failedStage.getOrder());
        }

        pipelineExecution.setStatus(PipelineStatus.FAILED);
    }

    private void handleStepSuccess(PipelineExecution pipelineExecution, PipelineExecution.StageExecution currentStage) {
        log.info("Handling step success for stage {} in pipeline {}", currentStage.getStageName(), pipelineExecution.getId());

        boolean allStepsCompleted = checkAllStepsCompleted(currentStage);
        if (allStepsCompleted) {
            currentStage.setStatus(PipelineStepExecutionStatus.SUCCESS);
            currentStage.setEndTime(LocalDateTime.now());

            boolean allStagesCompleted = checkAllStagesCompleted(pipelineExecution);
            pipelineExecution.setStatus(allStagesCompleted ? PipelineStatus.SUCCESS : PipelineStatus.IN_PROGRESS);
        } else {
            currentStage.setStatus(PipelineStepExecutionStatus.IN_PROGRESS);
            executeNextStep(currentStage);
        }
    }

    private boolean checkAllStepsCompleted(PipelineExecution.StageExecution stageExecution) {
        if (stageExecution.getTemplate() == null || CollectionUtils.isEmpty(stageExecution.getTemplate().getSteps())) {
            return true;
        }
        return stageExecution.getTemplate().getSteps().stream()
                .filter(step -> !step.isSkipStep())
                .allMatch(step -> step.getStatus() == StepExecutionStatus.COMPLETED);
    }

    private boolean checkAllStagesCompleted(PipelineExecution pipelineExecution) {
        return pipelineExecution.getStageExecutions().stream()
                .filter(stage -> !"init".equalsIgnoreCase(stage.getStageName()))
                .allMatch(stage -> stage.getStatus() == PipelineStepExecutionStatus.SUCCESS);
    }

    private void executeNextStep(PipelineExecution.StageExecution stageExecution) {
        if (stageExecution.getTemplate() == null || CollectionUtils.isEmpty(stageExecution.getTemplate().getSteps())) {
            return;
        }

        List<Template.Step> steps = stageExecution.getTemplate().getSteps().stream()
                .sorted(Comparator.comparingInt(Template.Step::getOrder))
                .toList();

        for (Template.Step step : steps) {
            if (step.getStatus() == StepExecutionStatus.CREATED || step.getStatus() == StepExecutionStatus.IN_PROGRESS) {
                if (!step.isSkipStep()) {
                    step.setStatus(StepExecutionStatus.IN_PROGRESS);
                    log.info("Executing next step {} in stage {}", step.getStepName(), stageExecution.getStageName());
                }
                break;
            }
        }
    }

    private void skipRemainingStages(PipelineExecution pipelineExecution, int failedStageOrder) {
        log.info("Skipping remaining stages after failed stage order {} in pipeline {}", failedStageOrder, pipelineExecution.getId());

        pipelineExecution.getStageExecutions().stream()
                .filter(stage -> stage.getOrder() > failedStageOrder)
                .forEach(stage -> {
                    stage.setStatus(PipelineStepExecutionStatus.SKIPPED);
                    stage.setEndTime(LocalDateTime.now());
                    if (stage.getTemplate() != null && !CollectionUtils.isEmpty(stage.getTemplate().getSteps())) {
                        stage.getTemplate().getSteps().forEach(step -> {
                            if (step.getStatus() != StepExecutionStatus.COMPLETED && step.getStatus() != StepExecutionStatus.FAILED) {
                                step.setStatus(StepExecutionStatus.SKIPPED);
                            }
                        });
                    }
                });
    }

    private PipelineExecution.StageExecution findBuildStageExecution(PipelineExecution pipelineExecution, String stageExecutionId) {
        if (StringUtils.hasText(stageExecutionId)) {
            return pipelineExecution.getStageExecutions().stream()
                    .filter(stage -> stageExecutionId.equals(stage.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return pipelineExecution.getStageExecutions().stream()
                .filter(stage -> "build".equalsIgnoreCase(stage.getStageName()))
                .findFirst()
                .orElse(null);
    }

    private boolean isTerminal(PipelineStepExecutionStatus status) {
        return status == PipelineStepExecutionStatus.SUCCESS
                || status == PipelineStepExecutionStatus.FAILED
                || status == PipelineStepExecutionStatus.CANCELLED
                || status == PipelineStepExecutionStatus.ERROR;
    }

    private PipelineStepExecutionStatus toPipelineStageStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return PipelineStepExecutionStatus.IN_PROGRESS;
        }
        return switch (status.toUpperCase()) {
            case "SUCCESS" -> PipelineStepExecutionStatus.SUCCESS;
            case "FAILURE", "FAILED" -> PipelineStepExecutionStatus.FAILED;
            case "CANCELLED", "ABORTED" -> PipelineStepExecutionStatus.CANCELLED;
            case "ERROR" -> PipelineStepExecutionStatus.ERROR;
            default -> PipelineStepExecutionStatus.IN_PROGRESS;
        };
    }

    private StepExecutionStatus toStepStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return StepExecutionStatus.IN_PROGRESS;
        }
        return switch (status.toUpperCase()) {
            case "SUCCESS" -> StepExecutionStatus.COMPLETED;
            case "FAILURE", "FAILED", "CANCELLED", "ABORTED", "ERROR" -> StepExecutionStatus.FAILED;
            default -> StepExecutionStatus.IN_PROGRESS;
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BuildUpdateMessage(
            @JsonProperty("build_id") String buildId,
            @JsonProperty("status") String status,
            @JsonProperty("event") String event,
            @JsonProperty("data") BuildUpdateData data
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BuildUpdateData(
            @JsonProperty("pipeline_execution_id") String pipelineExecutionId,
            @JsonProperty("stage_execution_id") String stageExecutionId
    ) {}
}
