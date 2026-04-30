package com.swadeshitech.prodhub.integration.vault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaultApiService {

    @Value("${vault.uri}")
    private String vaultUri;

    private String currentToken;

    private final ObjectMapper objectMapper;
    private static final java.time.Duration TIMEOUT = java.time.Duration.ofSeconds(10);
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    @Scheduled(fixedRate = 3600000) // Every 1 hour
    @PostConstruct
    public void loginToVault() {
        log.info("Attempting Vault Login via GCP Workload Identity...");
        try {
            // 1. Get Google ID Token from Metadata Server
            String metadataUri = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience=vault/prodhub-app-role";
            log.info("Step 1: Fetching JWT from Metadata Server: {}", metadataUri);
            HttpRequest metadataRequest = HttpRequest.newBuilder()
                    .uri(URI.create(metadataUri))
                    .header("Metadata-Flavor", "Google")
                    .GET()
                    .build();

            HttpResponse<String> jwtResponse = httpClient.send(metadataRequest, HttpResponse.BodyHandlers.ofString());
            if (jwtResponse.statusCode() != 200) {
                log.error("Step 1 Failed: Metadata Server returned status {}. Body: {}", jwtResponse.statusCode(), jwtResponse.body());
                return;
            }
            String jwt = jwtResponse.body();
            log.info("Step 1 Success: JWT retrieved (length: {})", jwt.length());

            // 2. Exchange for Vault Token
            String loginUri = vaultUri + "/v1/auth/gcp/login";
            log.info("Step 2: Exchanging JWT for Vault Token at: {}", loginUri);
            Map<String, String> loginPayload = Map.of(
                    "role", "prodhub-app-role",
                    "jwt", jwt
            );

            HttpRequest vaultLogin = HttpRequest.newBuilder()
                    .uri(URI.create(loginUri))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginPayload)))
                    .build();

            HttpResponse<String> loginResponse = httpClient.send(vaultLogin, HttpResponse.BodyHandlers.ofString());

            if (loginResponse.statusCode() == 200) {
                JsonNode loginJson = objectMapper.readTree(loginResponse.body());
                this.currentToken = loginJson.path("auth").path("client_token").asText();
                log.info("Step 2 Success: Vault token refreshed successfully.");
            } else {
                log.error("Step 2 Failed: Vault login returned status {}. Body: {}", loginResponse.statusCode(), loginResponse.body());
            }
        } catch (Exception e) {
            log.error("Critical error connecting to Vault: {}", e.getMessage());
        }
    }

    public void storeSecret(VaultRequest vaultRequest) {
        if (this.currentToken == null) {
            log.error("Cannot store secret: Vault token is null. Attempting re-login...");
            loginToVault();
            if (this.currentToken == null) return;
        }
        String vaultKvUrl = vaultUri + "/v1/secret/data/" + vaultRequest.getCredentialPath();
        try {
            String jsonPayload = objectMapper.writeValueAsString(Map.of("data", vaultRequest.getData()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(vaultKvUrl))
                    .header("X-Vault-Token", this.currentToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("Secret written successfully at path: {}", vaultRequest.getCredentialPath());
            } else {
                log.error("Failed to write secret. Status code: {} Response: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Error storing secret in Vault: {}", e.getMessage());
        }
    }

    public Map<String, Object> getSecret(String path) {
        if (this.currentToken == null) {
            log.warn("Vault token is null. Attempting re-login...");
            loginToVault();
        }
        if (this.currentToken == null) {
            log.error("Vault token is still null after re-login attempt. Cannot fetch secret: {}", path);
            return null;
        }
        String vaultKvUrl = vaultUri + "/v1/secret/data/" + path;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(vaultKvUrl))
                    .header("X-Vault-Token", this.currentToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode dataNode = rootNode.path("data").path("data");
                return objectMapper.convertValue(dataNode, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } else {
                log.error("Failed to get secret. Status code: {} Response: {}", response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Error getting secret from Vault: {}", e.getMessage());
            return null;
        }
    }
}