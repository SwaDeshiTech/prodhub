package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.dto.ResourceDetailsRegisterRequest;
import com.swadeshitech.prodhub.dto.ResourceDetailsRegisterResponse;
import com.swadeshitech.prodhub.dto.ResourceDetailsResponse;
import com.swadeshitech.prodhub.entity.ResourceDetails;
import com.swadeshitech.prodhub.services.ResourceService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Override
    public ResourceDetailsRegisterResponse registerResource(ResourceDetailsRegisterRequest registerRequest) {

        ResourceDetails resourceDetails = new ResourceDetails();
        resourceDetails.setCloudProvider(registerRequest.getCloudProvider());
        resourceDetails.setMeta(registerRequest.getMeta());
        resourceDetails.setName(registerRequest.getName());
        resourceDetails.setResourceType(registerRequest.getResourceType());

        resourceDetails = writeTransactionService.saveResourceDetailsToRepository(resourceDetails);
        return mapEntityToDTO(resourceDetails);
    }

    @Override
    public List<ResourceDetailsResponse> getResourceListByProvider(String cloudProvider) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("cloudProvider", cloudProvider);

        List<ResourceDetails> resourceDetails = readTransactionService.findResourceDetailsByFilters(filters);

        List<ResourceDetailsResponse> response = new ArrayList<>();

        for (ResourceDetails resource : resourceDetails) {
            response.add(ResourceDetailsResponse.builder()
                    .cloudProvider(resource.getCloudProvider())
                    .meta(resource.getMeta())
                    .id(resource.getId())
                    .name(resource.getName())
                    .build());
        }

        return response;
    }

    private ResourceDetailsRegisterResponse mapEntityToDTO(ResourceDetails resourceDetails) {
        return ResourceDetailsRegisterResponse.builder()
                .cloudProvider(resourceDetails.getCloudProvider())
                .meta(resourceDetails.getMeta())
                .name(resourceDetails.getName())
                .build();
    }
}
