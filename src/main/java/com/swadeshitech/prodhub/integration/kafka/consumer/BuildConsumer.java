package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.entity.PipelineExecution;
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

        PipelineStepExecutionStatus pipelineStepExecutionStatus = toPipelineStageStatus(buildUpdateMessage.status());
        stageExecution.setStatus(pipelineStepExecutionStatus);
        if (isTerminal(pipelineStepExecutionStatus)) {
            stageExecution.setEndTime(LocalDateTime.now());
        }

        if (stageExecution.getTemplate() != null && !CollectionUtils.isEmpty(stageExecution.getTemplate().getSteps())) {
            stageExecution.getTemplate().getSteps().forEach(step -> {
                if ("build".equalsIgnoreCase(step.getStepName())) {
                    step.setStatus(toStepStatus(buildUpdateMessage.status()));
                }
            });
        }

        if (pipelineStepExecutionStatus == PipelineStepExecutionStatus.FAILED
                || pipelineStepExecutionStatus == PipelineStepExecutionStatus.ERROR) {
            pipelineExecution.setStatus(PipelineStatus.FAILED);
            return;
        }

        boolean allDone = pipelineExecution.getStageExecutions().stream()
                .filter(stage -> !"init".equalsIgnoreCase(stage.getStageName()))
                .allMatch(stage -> stage.getStatus() == PipelineStepExecutionStatus.SUCCESS);

        pipelineExecution.setStatus(allDone ? PipelineStatus.SUCCESS : PipelineStatus.IN_PROGRESS);
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
