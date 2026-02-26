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

    @Autowired
    CredentialProviderService credentialProviderService;

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

        Map<String, String> params = generateParamsForProvider(pipelineExecution, data, buildProfile, decodedData);

        BuildTriggerRequest request = BuildTriggerRequest.builder()
                .triggeredBy(pipelineExecution.getCreatedBy())
                .parameters(params)
                .refId(UuidUtil.generateRandomUuid())
                .build();

        Mono<BuildTriggerResponse> buildTriggerResponse = ciCaptainClient.triggerBuild(providerId, jobName, request);
        BuildTriggerResponse response = buildTriggerResponse.blockOptional().get();
        log.info("Printing ci-captain build response {}", response);
        return response;
    }

    private Map<String, String> generateParamsForProvider(PipelineExecution pipelineExecution, JsonNode data, Metadata buildProfile, String decodedData) {

        String commitId = String.valueOf(pipelineExecution.getMetaData().get("commitId"));
        String dockerImageHashValue = buildProfile.getApplication().getName().toLowerCase() + "-"
                + Base64Util.generate7DigitHash(decodedData) + ":" + commitId.substring(0, 7);

        String scmProviderId = data.path("scmId").asText();

        Map<String, String> params = new HashMap<>();
        params.put("BRANCH_NAME", data.path("branchName").asText());
        params.put("COMMIT_ID", commitId);
        params.put("BASE_IMAGE", data.path("baseImage").asText());
        params.put("BUILD_COMMAND", data.path("buildCommand").asText());
        params.put("REPO_URL", credentialProviderService.extractSCMURL(scmProviderId) + "/" + data.path("repo").asText());
        params.put("ARTIFACT_PATH", data.path("artifactPath").asText());
        params.put("JOB_TEMPLATE", data.path("buildTemplate").asText());
        params.put("SERVICE_NAME", buildProfile.getApplication().getName());
        params.put("PROFILE_PATH", data.path("profilePath").asText());
        params.put("DOCKER_IMAGE_HASH_VALUE", dockerImageHashValue);
        params.put("BUILD_PATH", data.path("buildPath").asText());

        return params;
    }
}
