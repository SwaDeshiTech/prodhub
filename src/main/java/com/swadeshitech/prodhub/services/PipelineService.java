package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.PaginatedResponse;
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

    void triggerNextStage(PipelineExecution pipelineExecution);

    void processBuildCompletion(String buildRefId, String buildStatus);

    PipelineExecutionDetailsDTO getPipelineExecutionDetails(String pipelineExecutionId);

    List<PipelineExecutionDetailsDTO> getPipelineExecutions(Map<String, Object> filters);

    PaginatedResponse<PipelineExecutionDetailsDTO> getPipelineExecutionsPaginated(
            Map<String, Object> filters,
            Integer page,
            Integer size,
            String sortBy,
            String order);

    void syncPipelineStatus(String pipelineExecutionId, String forceSync);
}
