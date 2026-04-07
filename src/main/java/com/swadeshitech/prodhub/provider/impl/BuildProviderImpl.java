package com.swadeshitech.prodhub.provider.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.cicaptain.config.CiCaptainClient;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerRequest;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerResponse;
import com.swadeshitech.prodhub.provider.BuildProvider;
import com.swadeshitech.prodhub.utils.Base64Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class BuildProviderImpl implements BuildProvider {

    @Autowired
    CiCaptainClient ciCaptainClient;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public BuildTriggerResponse triggerBuild(PipelineExecution pipelineExecution, Metadata buildProfile,
            Map<String, String> values) {
        String providerId;
        try {
            JsonNode data = objectMapper.readTree(Base64Util.convertToPlainText(buildProfile.getData()));
            providerId = data.path("buildProviderId").asText();
        } catch (JsonProcessingException e) {
            log.error("Failed to parse build profile data for {}", buildProfile.getName(), e);
            throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
        }

        PipelineExecution.StageExecution buildStageExecution = pipelineExecution.getStageExecutions().stream()
                .filter(stage -> stage.getStageName().equalsIgnoreCase("build"))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PIPELINE_EXECUTION_COULD_NOT_BE_CREATED));

        BuildTriggerRequest request = BuildTriggerRequest.builder()
                .providerId(providerId)
                .pipelineExecutionId(pipelineExecution.getId())
                .stageExecutionId(buildStageExecution.getId())
                .triggeredBy(pipelineExecution.getCreatedBy())
                .build();

        Mono<BuildTriggerResponse> buildTriggerResponse = ciCaptainClient.triggerBuild(request);
        BuildTriggerResponse response = buildTriggerResponse.blockOptional().get();
        log.info("Printing ci-captain build response {}", response);
        return response;
    }
}
