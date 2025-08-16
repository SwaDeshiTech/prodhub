package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.swadeshitech.prodhub.dto.BuildProviderResponse;
import com.swadeshitech.prodhub.dto.BuilderProviderRequest;
import com.swadeshitech.prodhub.entity.BuildProvider;
import com.swadeshitech.prodhub.entity.Constants;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.BuildProviderService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BuildProviderServiceImpl implements BuildProviderService {

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Override
    public BuildProviderResponse onboardBuildProvider(BuilderProviderRequest request) {

        com.swadeshitech.prodhub.enums.BuildProvider providerEnum = com.swadeshitech.prodhub.enums.BuildProvider
                .fromDisplayName(request.getName());

        if (null == providerEnum) {
            log.error("Invalid build provider name: {}", request.getName());
            throw new CustomException(ErrorCode.INVALID_BUILD_PROVIDER_NAME);
        }

        BuildProvider buildProvider = BuildProvider.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .metaData(request.getMetaData())
                .buildProviderType(providerEnum)
                .build();

        buildProvider = writeTransactionService.saveBuildProviderToRepository(buildProvider);
        log.info("Build provider onboarded successfully with ID: {}", buildProvider.getId());

        return convertToResponse(buildProvider);
    }

    @Override
    public BuildProviderResponse getBuildProvider(String providerId) {

        Map<String, Object> filters = new HashMap<>();
        filters.put("_id", new ObjectId(providerId));

        List<BuildProvider> buildProviders = readTransactionService.findBuildProviderDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(buildProviders)) {
            log.error("No build provider found with ID: {}", providerId);
            throw new CustomException(ErrorCode.BUILD_PROVIDER_NOT_FOUND);
        }

        return convertToResponse(buildProviders.get(0));
    }

    @Override
    public BuildProviderResponse updateBuildProvider(String providerId, BuilderProviderRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateBuildProvider'");
    }

    @Override
    public List<BuildProviderResponse> getAllBuildProviders() {

        Constants constants = readTransactionService.getConstantByName("buildProvider");
        log.info(null != constants ? "Fetched build provider constants: {}" : "No build provider constants found",
                constants);
        if (CollectionUtils.isEmpty(constants.getValues())) {
            log.error("No build provider found with active status");
            throw new CustomException(ErrorCode.BUILD_PROVIDER_NOT_FOUND);
        }

        List<BuildProviderResponse> responses = new ArrayList<>();

        for (String provider : constants.getValues()) {
            responses.add(BuildProviderResponse.builder()
                    .id(provider)
                    .name(provider)
                    .location("/dashboard/onboarding/" + provider)
                    .build());
        }

        return responses;
    }

    @Override
    public List<BuildProviderResponse> registeredBuildProviders() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("isActive", true);

        List<BuildProvider> buildProviders = readTransactionService.findBuildProviderDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(buildProviders)) {
            log.error("No build provider found with active status");
            throw new CustomException(ErrorCode.BUILD_PROVIDER_NOT_FOUND);
        }

        return buildProviders.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private BuildProviderResponse convertToResponse(BuildProvider buildProvider) {
        return BuildProviderResponse.builder()
                .id(buildProvider.getId().toString())
                .name(buildProvider.getName())
                .isActive(buildProvider.isActive())
                .description(buildProvider.getDescription())
                .metaData(buildProvider.getMetaData())
                .createdBy(buildProvider.getCreatedBy())
                .createdTime(buildProvider.getCreatedTime())
                .lastModifiedBy(buildProvider.getLastModifiedBy())
                .lastModifiedTime(buildProvider.getLastModifiedTime())
                .buildProviderType(buildProvider.getBuildProviderType().toString())
                .build();
    }

    @Override
    public String removeBuildProvider(String providerId) {
        writeTransactionService.removeBuildProviderFromRepository(providerId);
        log.info("Build provider with ID: {} has been successfully removed", providerId);
        return "Build provider with ID: " + providerId + " has been successfully removed";
    }
}
