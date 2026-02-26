package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.PipelineExecutionRequest;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PipelineService {

    String schedulePipelineExecution(PipelineExecutionRequest request);

    PipelineExecution createPipelineExecution(String pipelineTemplateName, String metaDataId);

    void startPipelineExecution(PipelineExecution pipelineExecution);
}
