package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.swadeshitech.prodhub.dto.CloudProviderRegisterRequest;
import com.swadeshitech.prodhub.dto.CloudProviderRegisterResponse;
import com.swadeshitech.prodhub.dto.CloudProviderResponse;
import com.swadeshitech.prodhub.entity.CloudProvider;
import com.swadeshitech.prodhub.entity.Constants;
import com.swadeshitech.prodhub.enums.CloudProviderState;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.dto.CloudProviderDetailsResponse;
import com.swadeshitech.prodhub.services.CloudProviderService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CloudProviderServiceImpl implements CloudProviderService {

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Override
    public List<CloudProviderResponse> cloudProviderList() {

        Constants constants = readTransactionService.getConstantByName("cloudProvider");
        List<CloudProviderResponse> cloudProviderResponses = new ArrayList<>();
        for (String provider : constants.getValues()) {
            cloudProviderResponses.add(CloudProviderResponse.builder().name(provider)
                    .location("/dashboard/connect/onboarding/" + provider).build());
        }
        return cloudProviderResponses;
    }

    @Override
    public CloudProviderRegisterResponse registerCloudProvider(CloudProviderRegisterRequest registerRequest) {

        CloudProvider cloudProvider = new CloudProvider();
        cloudProvider.setActive(true);
        cloudProvider.setDescription(registerRequest.getDescription());
        cloudProvider.setName(registerRequest.getName());
        cloudProvider.setMetaData(registerRequest.getMetaData());
        cloudProvider.setState(CloudProviderState.ONBOARDED);

        cloudProvider = writeTransactionService.saveCloudProviderToRepository(cloudProvider);

        return mapEntityToDTO(cloudProvider);
    }

    private CloudProviderRegisterResponse mapEntityToDTO(CloudProvider cloudProvider) {
        return CloudProviderRegisterResponse.builder().id(cloudProvider.getId()).isActive(cloudProvider.isActive())
                .name(cloudProvider.getName()).state(cloudProvider.getState()).build();
    }

    @Override
    public List<CloudProviderResponse> registeredCloudProviderList() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("isActive", true);

        List<CloudProvider> providers = readTransactionService.findCloudProvidersByFilters(filters);

        List<CloudProviderResponse> cloudProviderResponses = new ArrayList<>();

        for (CloudProvider cloudProvider : providers) {
            cloudProviderResponses
                    .add(CloudProviderResponse.builder().id(cloudProvider.getId()).name(cloudProvider.getName())
                            .description(cloudProvider.getDescription()).isActive(cloudProvider.isActive()).build());
        }

        return cloudProviderResponses;
    }

    @Override
    public CloudProviderDetailsResponse getCloudProviderDetails(String id) {

        Map<String, Object> filters = new HashMap<>();
        filters.put("id", id);

        List<CloudProvider> providers = readTransactionService.findCloudProvidersByFilters(filters);
        if (CollectionUtils.isEmpty(providers)) {
            throw new CustomException(ErrorCode.CLOUD_PROVIDER_NOT_FOUND);
        }

        return CloudProviderDetailsResponse.builder().description(providers.get(0).getDescription())
                .id(providers.get(0).getId()).name(providers.get(0).getName()).metaData(providers.get(0).getMetaData())
                .build();
    }

    @Override
    public String deleteCloudProvider(String id) {
        writeTransactionService.removeCloudProviderFromRepository(id);
        return "Successfully deleted the cloud provider";
    }
}
