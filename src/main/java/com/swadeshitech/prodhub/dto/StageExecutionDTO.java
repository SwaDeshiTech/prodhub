package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;

import com.swadeshitech.prodhub.enums.PipelineStepExecutionStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StageExecutionDTO {
    private String id;
    private String stageName;
    private TemplateResponse template;
    private PipelineStepExecutionStatus status;
    private int order;
    private boolean stopOnFailure;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
