package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.CredentialProviderFilter;
import com.swadeshitech.prodhub.dto.CredentialProviderRequest;
import com.swadeshitech.prodhub.dto.CredentialProviderResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface CredentialProviderService {

    CredentialProviderResponse onboardCredentialProvider(CredentialProviderRequest request);

    CredentialProviderResponse credentialProviderDetails(String serviceId, String credentialId);

    CredentialProviderResponse credentialProviderDetails(String credentialId);

    CredentialProviderResponse uiCredentialProviderDetails(String serviceId, String credentialId);

    CredentialProviderResponse uiCredentialProviderDetails(String credentialId);

    List<CredentialProviderResponse> credentialProviders(CredentialProviderFilter credentialProviderFilter);

    List<DropdownDTO> getCredentialProvidersByType(String type);

    String extractSCMURL(String id);

    String extractRegistryURL(String id);

    CredentialProviderResponse updateCredentialProvider(String credentialId, CredentialProviderRequest request);

    Map<String, Object> syncCredentials(String scmCredentialId, List<String> buildProviderIds);
}
