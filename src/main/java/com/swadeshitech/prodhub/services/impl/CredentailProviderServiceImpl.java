package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.constant.Constants;
import com.swadeshitech.prodhub.dto.CredentialProviderRequest;
import com.swadeshitech.prodhub.dto.CredentialProviderResponse;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.CredentialProvider;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.vault.VaultApiService;
import com.swadeshitech.prodhub.integration.vault.VaultRequest;
import com.swadeshitech.prodhub.integration.vault.VaultResponse;
import com.swadeshitech.prodhub.services.CredentialProviderService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CredentailProviderServiceImpl implements CredentialProviderService {

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    VaultApiService vaultService;

    @Override
    public CredentialProviderResponse onboardCredentialProvider(CredentialProviderRequest request) {

        List<Application> applications = readTransactionService.findApplicationByFilters(
                Map.of("_id", new ObjectId(request.getServiceId())));
        if (applications.isEmpty()) {
            log.error("No application found for id: {}", request.getServiceId());
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        Application application = applications.get(0);

        String credentialPath = application.getName() + "/" + request.getName();
        Map<String, Object> creds = new HashMap<>();
        creds.put(request.getName(), request.getMetaData());

        VaultRequest vaultRequest = VaultRequest.builder()
                .credentialPath(credentialPath)
                .data(creds)
                .build();

        vaultService.storeSecret(vaultRequest);

        CredentialProvider credentialProvider = CredentialProvider.builder()
                .name(request.getName())
                .description(request.getDescription())
                .application(application)
                .credentialPath(credentialPath)
                .isActive(true)
                .credentialProvider(com.swadeshitech.prodhub.enums.CredentialProvider.fromDisplayName(request.getProvider()))
                .build();

        writeTransactionService.saveCredentialProviderToRepository(credentialProvider);

        return mapEntityToDTO(credentialProvider);
    }

    @Override
    public CredentialProviderResponse credentialProviderDetails(String serviceId, String credentialId) {

        List<Application> applications = readTransactionService.findApplicationByFilters(
                Map.of("_id", new ObjectId(serviceId)));
        if (applications.isEmpty()) {
            log.error("No application found for id: {}", serviceId);
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        Application application = applications.getFirst();

        List<CredentialProvider> credentialProviders = readTransactionService.findCredentialProviderByFilters(Map.of("_id", new ObjectId(credentialId), "application", application));

        if(credentialProviders.isEmpty()) {
            log.error("No credential provider found for serviceId: {} and credentialId: {}", serviceId, credentialId);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_NOT_FOUND);
        }

        CredentialProvider credentialProvider = credentialProviders.getFirst();

        return mapEntityToDTO(credentialProvider);
    }

    @Override
    public List<CredentialProviderResponse> credentialProviders(String type) {
        return List.of();
    }

    private CredentialProviderResponse mapEntityToDTO(CredentialProvider credentialProvider) {
        return CredentialProviderResponse.builder()
                .id(credentialProvider.getId())
                .description(credentialProvider.getDescription())
                .credentialPath(credentialProvider.getCredentialPath())
                .isActive(credentialProvider.isActive())
                .type(credentialProvider.getCredentialProvider().getDisplayName())
                .serviceName(credentialProvider.getApplication().getName())
                .createdBy(credentialProvider.getCreatedBy())
                .createdTime(credentialProvider.getCreatedTime())
                .lastModifiedBy(credentialProvider.getLastModifiedBy())
                .lastModifiedTime(credentialProvider.getLastModifiedTime())
                .build();
    }
}
