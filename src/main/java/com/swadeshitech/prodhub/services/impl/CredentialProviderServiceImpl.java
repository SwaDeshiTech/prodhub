package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.CredentialProviderFilter;
import com.swadeshitech.prodhub.dto.CredentialProviderRequest;
import com.swadeshitech.prodhub.dto.CredentialProviderResponse;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.CredentialProvider;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.vault.VaultApiService;
import com.swadeshitech.prodhub.integration.vault.VaultRequest;
import com.swadeshitech.prodhub.services.CredentialProviderService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CredentialProviderServiceImpl implements CredentialProviderService {

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

        com.swadeshitech.prodhub.enums.CredentialProvider credentialProviderType = com.swadeshitech.prodhub.enums.CredentialProvider.fromDisplayName(request.getProvider());

        CredentialProvider credentialProvider = CredentialProvider.builder()
                .name(request.getName())
                .description(request.getDescription())
                .application(application)
                .credentialPath(credentialPath)
                .isActive(true)
                .credentialProvider(credentialProviderType)
                .credentialProviderType(credentialProviderType.getType())
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

        CredentialProviderResponse response = mapEntityToDTO(credentialProvider);

        Map<String, Object> vaultResponse = vaultService.getSecret(credentialProvider.getCredentialPath());

        response.setCredentialMetadata(vaultResponse != null ? vaultResponse.toString() : null);

        return response;
    }

    @Override
    public List<CredentialProviderResponse> credentialProviders(CredentialProviderFilter credentialProviderFilter) {

        Map<String, Object> filters = createFilterObject(credentialProviderFilter);

        List<CredentialProvider> credentialProviders = readTransactionService.findCredentialProviderByFilters(filters);
        if(CollectionUtils.isEmpty(credentialProviders)) {
            log.info("Fail to fetch credential providers list {}", filters);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_LIST_NOT_FOUND);
        }

        List<CredentialProviderResponse> credentialProviderResponses = new ArrayList<>();

        for(CredentialProvider credentialProvider : credentialProviders) {
            credentialProviderResponses.add(mapEntityToDTO(credentialProvider));
        }

        return credentialProviderResponses;
    }

    private Map<String, Object> createFilterObject(CredentialProviderFilter credentialProviderFilter) {

        Map<String, Object> filters = new HashMap<>();

        if(StringUtils.isNoneBlank(credentialProviderFilter.getApplicationId())) {
            List<Application> applications = readTransactionService.findApplicationByFilters(
                    Map.of("_id", new ObjectId(credentialProviderFilter.getApplicationId())));
            if (applications.isEmpty()) {
                log.error("No application found for id: {}", credentialProviderFilter.getApplicationId());
                throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
            }
            Application application = applications.getFirst();
            filters.put("application", application);
        }

        if(StringUtils.isNoneBlank(credentialProviderFilter.getType())) {
            filters.put("credentialProvider", com.swadeshitech.prodhub.enums.CredentialProvider.fromDisplayName(credentialProviderFilter.getType()));
        }

        if(StringUtils.isNoneBlank(credentialProviderFilter.getName())) {
            filters.put("name", credentialProviderFilter.getName());
        }

        filters.put("isActive", true);

        return filters;
    }

    private CredentialProviderResponse mapEntityToDTO(CredentialProvider credentialProvider) {
        return CredentialProviderResponse.builder()
                .id(credentialProvider.getId())
                .name(credentialProvider.getName())
                .description(credentialProvider.getDescription())
                .credentialPath(credentialProvider.getCredentialPath())
                .isActive(credentialProvider.isActive())
                .type(credentialProvider.getCredentialProvider().getDisplayName())
                .serviceName(credentialProvider.getApplication().getName())
                .serviceId(credentialProvider.getApplication().getId())
                .createdBy(credentialProvider.getCreatedBy())
                .createdTime(credentialProvider.getCreatedTime())
                .lastModifiedBy(credentialProvider.getLastModifiedBy())
                .lastModifiedTime(credentialProvider.getLastModifiedTime())
                .build();
    }
}
