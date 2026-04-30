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
            HttpRequest metadataRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience=vault/prodhub-app-role"))
                    .header("Metadata-Flavor", "Google")
                    .GET()
                    .build();

            String jwt = httpClient.send(metadataRequest, HttpResponse.BodyHandlers.ofString()).body();

            // 2. Exchange for Vault Token
            Map<String, String> loginPayload = Map.of(
                    "role", "prodhub-app-role",
                    "jwt", jwt
            );

            HttpRequest vaultLogin = HttpRequest.newBuilder()
                    .uri(URI.create(vaultUri + "/v1/auth/gcp/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginPayload)))
                    .build();

            HttpResponse<String> loginResponse = httpClient.send(vaultLogin, HttpResponse.BodyHandlers.ofString());

            if (loginResponse.statusCode() == 200) {
                JsonNode loginJson = objectMapper.readTree(loginResponse.body());
                this.currentToken = loginJson.path("auth").path("client_token").asText();
                log.info("Vault token refreshed successfully.");
            } else {
                log.error("Vault login failed: {}", loginResponse.body());
            }
        } catch (Exception e) {
            log.error("Critical error connecting to Vault: {}", e.getMessage());
        }
    }

    public void storeSecret(VaultRequest vaultRequest) {
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
        if (currentToken == null) {
            loginToVault();
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