package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.PipelineExecutionRequest;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PipelineService {

    String schedulePipelineExecution(PipelineExecutionRequest request);

    PipelineExecution createPipelineExecution(PipelineExecutionRequest request);

    void startPipelineExecution(PipelineExecution pipelineExecution);
}
