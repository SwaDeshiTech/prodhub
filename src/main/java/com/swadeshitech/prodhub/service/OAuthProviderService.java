package com.swadeshitech.prodhub.service;

import com.swadeshitech.prodhub.entity.OAuthProvider;
import com.swadeshitech.prodhub.repository.OAuthProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OAuthProviderService {

    @Autowired
    private OAuthProviderRepository oauthProviderRepository;

    public List<OAuthProvider> getAllActiveProviders() {
        return oauthProviderRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    public Optional<OAuthProvider> getProviderByType(String providerType) {
        return oauthProviderRepository.findByProviderTypeAndIsActive(providerType, true);
    }

    public Optional<OAuthProvider> getProviderById(String id) {
        return oauthProviderRepository.findById(id);
    }

    public OAuthProvider createProvider(OAuthProvider provider) {
        return oauthProviderRepository.save(provider);
    }

    public OAuthProvider updateProvider(String id, OAuthProvider provider) {
        provider.setId(id);
        return oauthProviderRepository.save(provider);
    }

    public void deleteProvider(String id) {
        oauthProviderRepository.deleteById(id);
    }

    public OAuthProvider getProviderConfig(String providerType) {
        return oauthProviderRepository.findByProviderTypeAndIsActive(providerType, true)
                .orElseThrow(() -> new RuntimeException("OAuth provider not found: " + providerType));
    }
}
