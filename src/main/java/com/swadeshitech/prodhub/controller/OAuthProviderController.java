package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.entity.OAuthProvider;
import com.swadeshitech.prodhub.service.OAuthProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/oauth-providers")
public class OAuthProviderController {

    @Autowired
    private OAuthProviderService oauthProviderService;

    @GetMapping
    public ResponseEntity<List<OAuthProvider>> getAllActiveProviders() {
        List<OAuthProvider> providers = oauthProviderService.getAllActiveProviders();
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/type/{providerType}")
    public ResponseEntity<OAuthProvider> getProviderByType(@PathVariable String providerType) {
        try {
            OAuthProvider provider = oauthProviderService.getProviderConfig(providerType);
            // Return without sensitive data
            OAuthProvider safeProvider = OAuthProvider.builder()
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
            return ResponseEntity.ok(safeProvider);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/config/{providerType}")
    public ResponseEntity<OAuthProvider> getProviderConfig(@PathVariable String providerType) {
        try {
            OAuthProvider provider = oauthProviderService.getProviderConfig(providerType);
            return ResponseEntity.ok(provider);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OAuthProvider> getProviderById(@PathVariable String id) {
        Optional<OAuthProvider> provider = oauthProviderService.getProviderById(id);
        return provider.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OAuthProvider> createProvider(@RequestBody OAuthProvider provider) {
        OAuthProvider createdProvider = oauthProviderService.createProvider(provider);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProvider);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OAuthProvider> updateProvider(@PathVariable String id, @RequestBody OAuthProvider provider) {
        try {
            OAuthProvider updatedProvider = oauthProviderService.updateProvider(id, provider);
            return ResponseEntity.ok(updatedProvider);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable String id) {
        try {
            oauthProviderService.deleteProvider(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
