package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.OAuthProviderResponse;
import com.swadeshitech.prodhub.dto.Response;
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
    public ResponseEntity<List<OAuthProviderResponse>> getAllProviders() {
        List<OAuthProviderResponse> providers = oauthProviderService.getAllActiveProvidersSafe();
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/active")
    public ResponseEntity<Response> getAllActiveProvidersSafe() {
        List<OAuthProviderResponse> providers = oauthProviderService.getAllActiveProvidersSafe();
        Response response = Response.builder()
                .message("Active OAuth providers fetched successfully")
                .httpStatus(HttpStatus.OK)
                .response(providers)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{providerType}")
    public ResponseEntity<Response> getProviderByType(@PathVariable String providerType) {
        try {
            OAuthProvider provider = oauthProviderService.getProviderConfig(providerType);
            OAuthProvider safeProvider = oauthProviderService.toSafeProvider(provider);
            Response response = Response.builder()
                    .message("OAuth provider fetched successfully")
                    .httpStatus(HttpStatus.OK)
                    .response(safeProvider)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .message("OAuth provider not found")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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
    public ResponseEntity<Response> getProviderById(@PathVariable String id) {
        Optional<OAuthProvider> provider = oauthProviderService.getProviderById(id);
        if (provider.isPresent()) {
            Response response = Response.builder()
                    .message("OAuth provider fetched successfully")
                    .httpStatus(HttpStatus.OK)
                    .response(provider.get())
                    .build();
            return ResponseEntity.ok(response);
        } else {
            Response response = Response.builder()
                    .message("OAuth provider not found")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Response> createProvider(@RequestBody OAuthProvider provider) {
        OAuthProvider createdProvider = oauthProviderService.createProvider(provider);
        Response response = Response.builder()
                .message("OAuth provider created successfully")
                .httpStatus(HttpStatus.CREATED)
                .response(createdProvider)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateProvider(@PathVariable String id, @RequestBody OAuthProvider provider) {
        try {
            OAuthProvider updatedProvider = oauthProviderService.updateProvider(id, provider);
            Response response = Response.builder()
                    .message("OAuth provider updated successfully")
                    .httpStatus(HttpStatus.OK)
                    .response(updatedProvider)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .message("OAuth provider not found")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteProvider(@PathVariable String id) {
        try {
            oauthProviderService.deleteProvider(id);
            Response response = Response.builder()
                    .message("OAuth provider deleted successfully")
                    .httpStatus(HttpStatus.NO_CONTENT)
                    .build();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        } catch (Exception e) {
            Response response = Response.builder()
                    .message("OAuth provider not found")
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
