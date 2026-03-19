package com.swadeshitech.prodhub.provider.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.constant.Constants;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.cicaptain.config.CiCaptainClient;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerRequest;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerResponse;
import com.swadeshitech.prodhub.provider.BuildProvider;
import com.swadeshitech.prodhub.services.CredentialProviderService;
import com.swadeshitech.prodhub.utils.Base64Util;
import com.swadeshitech.prodhub.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
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
        String jobName = buildProfile.getApplication().getName() + "-" + buildProfile.getName()
                .split(Constants.CLONE_METADATA_DELIMITER)[0];
        JsonNode data;
        String ephemeralEnvironment = String.valueOf(pipelineExecution.getMetaData().get("ephemeralEnvironment"));
        String decodedData;

        try {
            decodedData = Base64Util.convertToPlainText(buildProfile.getData());
            data = objectMapper.readTree(decodedData);
            providerId = data.path("buildProviderId").asText();
        } catch (JsonProcessingException e) {
            log.error("Fail to read metadata of profile {}", buildProfile.getName());
            throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
        }

        if(StringUtils.hasText(ephemeralEnvironment)) {
            jobName += "-" + ephemeralEnvironment;
        }

        BuildTriggerRequest request = BuildTriggerRequest.builder()
                .triggeredBy(pipelineExecution.getCreatedBy())
                .refId(UuidUtil.generateRandomUuid())
                .build();

        Mono<BuildTriggerResponse> buildTriggerResponse = ciCaptainClient.triggerBuild(providerId, jobName, request);
        BuildTriggerResponse response = buildTriggerResponse.blockOptional().get();
        log.info("Printing ci-captain build response {}", response);
        return response;
    }
}
