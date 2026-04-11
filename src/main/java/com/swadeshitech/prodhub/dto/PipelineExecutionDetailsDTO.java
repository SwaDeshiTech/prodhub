package com.swadeshitech.prodhub.dto;

import java.util.List;
import java.util.Map;

import com.swadeshitech.prodhub.enums.PipelineStatus;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PipelineExecutionDetailsDTO {
    private String id;
    private PipelineStatus status;
    private List<StageExecutionDTO> stageExecutions;
    private Map<String, Object> metaData;
}
