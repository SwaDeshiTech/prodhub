package com.swadeshitech.prodhub.integration.vault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${vault.token}")
    private String vaultToken;

    private final ObjectMapper objectMapper;
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public void storeSecret(VaultRequest vaultRequest) {
        String vaultKvUrl = vaultUri + "/v1/secret/data/" + vaultRequest.getCredentialPath();
        try {
            String jsonPayload = objectMapper.writeValueAsString(Map.of("data", vaultRequest.getData()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(vaultKvUrl))
                    .header("X-Vault-Token", vaultToken)
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
        String vaultKvUrl = vaultUri + "/v1/secret/data/" + path;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(vaultKvUrl))
                    .header("X-Vault-Token", vaultToken)
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