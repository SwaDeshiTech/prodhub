package com.swadeshitech.prodhub.integration.storage;

import com.swadeshitech.prodhub.enums.CredentialProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for managing and retrieving storage providers based on provider type
 */
@Component
@Slf4j
public class StorageProviderFactory {

    private final Map<String, StorageProvider> providerMap;

    @Autowired
    public StorageProviderFactory(List<StorageProvider> providers) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(
                        StorageProvider::getProviderType,
                        Function.identity()
                ));
        log.info("Initialized storage providers: {}", providerMap.keySet());
    }

    /**
     * Gets the storage provider for the given provider type
     * 
     * @param providerType The provider type (e.g., "JFROG_ARTIFACTORY", "BLOB_STORAGE")
     * @return The storage provider implementation
     * @throws IllegalArgumentException if provider type is not supported
     */
    public StorageProvider getProvider(String providerType) {
        StorageProvider provider = providerMap.get(providerType);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported storage provider type: " + providerType);
        }
        return provider;
    }

    /**
     * Gets the storage provider based on CredentialProvider enum
     * 
     * @param credentialProvider The credential provider enum
     * @return The storage provider implementation
     */
    public StorageProvider getProvider(CredentialProvider credentialProvider) {
        return getProvider(credentialProvider.name());
    }

    /**
     * Checks if a provider type is supported
     * 
     * @param providerType The provider type to check
     * @return true if supported, false otherwise
     */
    public boolean isProviderSupported(String providerType) {
        return providerMap.containsKey(providerType);
    }
}
