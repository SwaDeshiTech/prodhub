package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.CloudProviderDetailsResponse;
import com.swadeshitech.prodhub.dto.CloudProviderRegisterRequest;
import com.swadeshitech.prodhub.dto.CloudProviderRegisterResponse;
import com.swadeshitech.prodhub.dto.CloudProviderResponse;

@Component
public interface CloudProviderService {

    public CloudProviderRegisterResponse registerCloudProvider(CloudProviderRegisterRequest registerRequest);

    public List<CloudProviderResponse> cloudProviderList();

    public List<CloudProviderResponse> registeredCloudProviderList();

    public CloudProviderDetailsResponse getCloudProviderDetails(String id);

    public String deleteCloudProvider(String id);
}
