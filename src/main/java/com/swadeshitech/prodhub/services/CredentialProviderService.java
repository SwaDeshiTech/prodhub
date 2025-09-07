package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.CredentialProviderRequest;
import com.swadeshitech.prodhub.dto.CredentialProviderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CredentialProviderService {

    public CredentialProviderResponse onboardCredentialProvider(CredentialProviderRequest request);

    public CredentialProviderResponse credentialProviderDetails(String serviceId, String credentialId);

    public List<CredentialProviderResponse> credentialProviders(String type);
}
