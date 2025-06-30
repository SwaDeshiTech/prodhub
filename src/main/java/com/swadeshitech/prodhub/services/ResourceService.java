package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.ResourceDetailsRegisterRequest;
import com.swadeshitech.prodhub.dto.ResourceDetailsRegisterResponse;
import com.swadeshitech.prodhub.dto.ResourceDetailsResponse;

@Component
public interface ResourceService {

    public ResourceDetailsRegisterResponse registerResource(ResourceDetailsRegisterRequest registerRequest);

    public List<ResourceDetailsResponse> getResourceListByProvider(String cloudProvider);
}
