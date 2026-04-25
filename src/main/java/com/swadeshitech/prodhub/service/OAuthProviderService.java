package com.swadeshitech.prodhub.service;

import com.swadeshitech.prodhub.dto.OAuthProviderResponse;
import com.swadeshitech.prodhub.entity.OAuthProvider;
import com.swadeshitech.prodhub.repository.OAuthProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OAuthProviderService {

    @Autowired
    private OAuthProviderRepository oauthProviderRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public List<OAuthProvider> getAllProviders() {
        return oauthProviderRepository.findAllByOrderBySortOrderAsc();
    }

    public List<OAuthProviderResponse> getAllActiveProvidersSafe() {
        List<OAuthProvider> providers = oauthProviderRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return providers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

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

    private OAuthProviderResponse toResponse(OAuthProvider provider) {
        return OAuthProviderResponse.builder()
                .id(provider.getId())
                .name(provider.getName())
                .providerType(provider.getProviderType())
                .displayName(provider.getDisplayName())
                .description(provider.getDescription())
                .isActive(provider.getIsActive())
                .isDefault(provider.getIsDefault())
                .redirectUrl(provider.getRedirectUrl())
                .scopes(provider.getScopes())
                .authUrl(provider.getAuthUrl())
                .tokenUrl(provider.getTokenUrl())
                .userInfoUrl(provider.getUserInfoUrl())
                .logoUrl(provider.getLogoUrl())
                .sortOrder(provider.getSortOrder())
                .createdTime(provider.getCreatedTime() != null ? provider.getCreatedTime().format(DATE_FORMATTER) : null)
                .lastModifiedTime(provider.getLastModifiedTime() != null ? provider.getLastModifiedTime().format(DATE_FORMATTER) : null)
                .build();
    }

    public OAuthProvider toSafeProvider(OAuthProvider provider) {
        return OAuthProvider.builder()
                .id(provider.getId())
                .name(provider.getName())
                .providerType(provider.getProviderType())
                .displayName(provider.getDisplayName())
                .description(provider.getDescription())
                .isActive(provider.getIsActive())
                .isDefault(provider.getIsDefault())
                .redirectUrl(provider.getRedirectUrl())
                .scopes(provider.getScopes())
                .authUrl(provider.getAuthUrl())
                .tokenUrl(provider.getTokenUrl())
                .userInfoUrl(provider.getUserInfoUrl())
                .logoUrl(provider.getLogoUrl())
                .sortOrder(provider.getSortOrder())
                .build();
    }
}
