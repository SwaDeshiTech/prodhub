package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.PipelineExecutionDetailsDTO;
import com.swadeshitech.prodhub.dto.PipelineExecutionRequest;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface PipelineService {

    String schedulePipelineExecution(PipelineExecutionRequest request);

    PipelineExecution createPipelineExecution(PipelineExecutionRequest request);

    void startPipelineExecution(PipelineExecution pipelineExecution);

    PipelineExecutionDetailsDTO getPipelineExecutionDetails(String pipelineExecutionId);

    List<PipelineExecutionDetailsDTO> getPipelineExecutions(Map<String, Object> filters);
}
