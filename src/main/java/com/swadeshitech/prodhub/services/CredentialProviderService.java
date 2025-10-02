package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.CredentialProviderFilter;
import com.swadeshitech.prodhub.dto.CredentialProviderRequest;
import com.swadeshitech.prodhub.dto.CredentialProviderResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CredentialProviderService {

    public CredentialProviderResponse onboardCredentialProvider(CredentialProviderRequest request);

    public CredentialProviderResponse credentialProviderDetails(String serviceId, String credentialId);

    public List<CredentialProviderResponse> credentialProviders(CredentialProviderFilter credentialProviderFilter);

    public List<DropdownDTO> getCredentialProvidersByType(String type);
}
