package com.swadeshitech.prodhub.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.dto.CredentialProviderFilter;
import com.swadeshitech.prodhub.dto.CredentialProviderRequest;
import com.swadeshitech.prodhub.dto.CredentialProviderResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CredentialProviderServiceImpl implements CredentialProviderService {

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    VaultApiService vaultService;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${github.baseURL}")
    String githubBaseURL;

    @Value("${cicaptain.baseURL:http://localhost:8081}")
    String ciCaptainURI;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public CredentialProviderResponse onboardCredentialProvider(CredentialProviderRequest request) {

        List<Application> applications = readTransactionService.findApplicationByFilters(
                Map.of("_id", new ObjectId(request.getServiceId())));
        if (applications.isEmpty()) {
            log.error("No application found for id: {}", request.getServiceId());
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        Application application = applications.getFirst();

        String credentialPath = String.join("_", application.getName().split("\\s+")) + "/" +
                String.join("_", request.getName().split("\\s+"));
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("secret", request.getMetaData());

        VaultRequest vaultRequest = VaultRequest.builder()
                .credentialPath(credentialPath)
                .data(credentials)
                .build();

        vaultService.storeSecret(vaultRequest);

        com.swadeshitech.prodhub.enums.CredentialProvider credentialProviderType = com.swadeshitech.prodhub.enums.CredentialProvider.fromValue(request.getProvider());

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

        response.setCredentialMetadata(vaultResponse != null ? vaultResponse.get("secret").toString() : null);

        return response;
    }

    @Override
    public CredentialProviderResponse credentialProviderDetails(String credentialId) {

        List<CredentialProvider> credentialProviders = readTransactionService.findByDynamicOrFilters(Map.of("_id", new ObjectId(credentialId)), CredentialProvider.class);
        if(CollectionUtils.isEmpty(credentialProviders)) {
            log.info("Fail to fetch credential providers list {}", credentialId);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_LIST_NOT_FOUND);
        }

        CredentialProvider credentialProvider = credentialProviders.getFirst();
        Map<String, Object> vaultResponse = vaultService.getSecret(credentialProvider.getCredentialPath());
        CredentialProviderResponse credentialProviderResponse = mapEntityToDTO(credentialProvider);
        if(credentialProvider.getCredentialProvider().equals(com.swadeshitech.prodhub.enums.CredentialProvider.K8S)) {
            credentialProviderResponse.setCredentialMetadata(vaultResponse != null ? vaultResponse.get("secret").toString() : null);
        }
        return credentialProviderResponse;
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

    @Override
    public List<DropdownDTO> getCredentialProvidersByType(String type) {

        Map<String, Object> filters = Map.of("credentialProviderType", type);
        List<CredentialProvider> credentialProviders = readTransactionService.findCredentialProviderByFilters(filters);
        if(CollectionUtils.isEmpty(credentialProviders)) {
            log.info("Fail to fetch credential providers list {}", filters);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_LIST_NOT_FOUND);
        }

        List<DropdownDTO> dropdownDTOS = new ArrayList<>();
        for(CredentialProvider provider : credentialProviders) {
            dropdownDTOS.add(DropdownDTO.builder()
                    .value(provider.getName() + " (" + provider.getCredentialProvider().getDisplayName() + ")")
                    .key(provider.getId())
                    .build());
        }
        return dropdownDTOS;
    }

    @Override
    public String extractSCMURL(String credentialId) {
        List<CredentialProvider> credentialProviders = readTransactionService.findByDynamicOrFilters(Map.of("_id", new ObjectId(credentialId)), CredentialProvider.class);
        if(CollectionUtils.isEmpty(credentialProviders)) {
            log.info("Fail to fetch credential providers list {}", credentialId);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_LIST_NOT_FOUND);
        }

        CredentialProvider credentialProvider = credentialProviders.getFirst();
        Map<String, Object> vaultResponse = vaultService.getSecret(credentialProvider.getCredentialPath());
        String scmStoredData = vaultResponse.get("secret").toString();
        try {
            JsonNode node = objectMapper.readTree(scmStoredData);
            return extractURLKey(credentialProvider, node);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse SCM data", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractURLKey(CredentialProvider credentialProvider, JsonNode scmData) {
        if (Objects.requireNonNull(credentialProvider.getCredentialProvider()) == com.swadeshitech.prodhub.enums.CredentialProvider.GITHUB) {
            if (org.springframework.util.StringUtils.hasText(scmData.path("github_org").asText())) {
                return githubBaseURL + "/" + scmData.path("github_org").asText();
            }
            if (org.springframework.util.StringUtils.hasText(scmData.path("github_owner").asText())) {
                return githubBaseURL + "/" + scmData.path("github_owner").asText();
            }
        }
        return null;
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
            filters.put("credentialProviderType", com.swadeshitech.prodhub.enums.CredentialProvider.fromType(credentialProviderFilter.getType()).getType());
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

    @Override
    public CredentialProviderResponse updateCredentialProvider(String credentialId, CredentialProviderRequest request) {
        log.info("Updating credential provider with id: {}", credentialId);

        List<CredentialProvider> credentialProviders = readTransactionService.findCredentialProviderByFilters(
                Map.of("_id", new ObjectId(credentialId)));
        
        if (credentialProviders.isEmpty()) {
            log.error("Credential provider not found with id: {}", credentialId);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_NOT_FOUND);
        }

        CredentialProvider credentialProvider = credentialProviders.getFirst();

        // Update name if provided
        if (StringUtils.isNotBlank(request.getName())) {
            credentialProvider.setName(request.getName());
        }

        // Update description if provided
        if (StringUtils.isNotBlank(request.getDescription())) {
            credentialProvider.setDescription(request.getDescription());
        }

        // Update metadata in vault if provided
        if (StringUtils.isNotBlank(request.getMetaData())) {
            String credentialPath = credentialProvider.getCredentialPath();
            Map<String, Object> credentials = new HashMap<>();
            credentials.put("secret", request.getMetaData());

            VaultRequest vaultRequest = VaultRequest.builder()
                    .credentialPath(credentialPath)
                    .data(credentials)
                    .build();

            vaultService.storeSecret(vaultRequest);
        }

        // Update active status if provided
        if (request.getIsActive() != null) {
            credentialProvider.setActive(request.getIsActive());
        }

        writeTransactionService.saveCredentialProviderToRepository(credentialProvider);

        log.info("Credential provider updated successfully with id: {}", credentialId);
        return mapEntityToDTO(credentialProvider);
    }

    @Override
    public Map<String, Object> syncCredentials(String scmCredentialId, List<String> buildProviderIds) {
        log.info("Syncing credentials from SCM provider {} to build providers {}", scmCredentialId, buildProviderIds);

        // Fetch SCM credential details
        List<CredentialProvider> scmCredentials = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", new ObjectId(scmCredentialId)), CredentialProvider.class);

        if (CollectionUtils.isEmpty(scmCredentials)) {
            log.error("SCM credential not found with id: {}", scmCredentialId);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_NOT_FOUND);
        }

        CredentialProvider scmCredential = scmCredentials.getFirst();

        // Get SCM credentials from vault
        Map<String, Object> vaultResponse = vaultService.getSecret(scmCredential.getCredentialPath());
        String scmStoredData = vaultResponse.get("secret").toString();

        // Parse SCM credentials
        Map<String, String> scmCredentialsMap = new HashMap<>();
        try {
            JsonNode node = objectMapper.readTree(scmStoredData);
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                scmCredentialsMap.put(field.getKey(), field.getValue().asText());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse SCM credentials", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // Prepare request for ci-captain
        Map<String, Object> ciCaptainRequest = new HashMap<>();
        ciCaptainRequest.put("scmProviderId", scmCredentialId);
        ciCaptainRequest.put("scmType", scmCredential.getCredentialProvider().getDisplayName());
        ciCaptainRequest.put("scmCredentials", scmCredentialsMap);
        ciCaptainRequest.put("buildProviderIds", buildProviderIds);

        // Call ci-captain to sync credentials
        String ciCaptainUrl = ciCaptainURI + "/api/v1/credentials/sync";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(ciCaptainRequest, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    ciCaptainUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                log.info("Credentials synced successfully: {}", responseBody);

                // Save sync result to credential provider metadata
                // You might want to add a new field to CredentialProvider entity to store sync history

                return responseBody != null ? responseBody : Map.of("status", "success");
            } else {
                log.error("Failed to sync credentials. Status: {}", response.getStatusCode());
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.error("Error calling ci-captain for credential sync", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
