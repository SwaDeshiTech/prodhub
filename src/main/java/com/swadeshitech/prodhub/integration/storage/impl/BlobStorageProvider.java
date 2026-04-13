package com.swadeshitech.prodhub.integration.storage.impl;

import com.swadeshitech.prodhub.integration.storage.FileUploadRequest;
import com.swadeshitech.prodhub.integration.storage.FileUploadResponse;
import com.swadeshitech.prodhub.integration.storage.StorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Azure Blob Storage provider implementation
 */
@Component
@Slf4j
public class BlobStorageProvider implements StorageProvider {

    private static final String PROVIDER_TYPE = "BLOB_STORAGE";

    @Override
    public FileUploadResponse uploadFile(FileUploadRequest request, Map<String, Object> credentials) {
        try {
            String storageAccountName = (String) credentials.get("storageAccountName");
            String containerName = (String) credentials.get("containerName");
            String sasToken = (String) credentials.get("sasToken");
            String accountKey = (String) credentials.get("accountKey");

            if (storageAccountName == null || containerName == null) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Missing required credentials: storageAccountName or containerName")
                        .build();
            }

            // Construct the blob URL
            String folderPath = request.getFolderPath() != null ? request.getFolderPath() : "";
            String blobUrl = String.format("https://%s.blob.core.windows.net/%s/%s/%s",
                    storageAccountName,
                    containerName,
                    folderPath,
                    request.getFileName());

            // Add SAS token or use account key for authentication
            String fullUrl = blobUrl;
            if (sasToken != null && !sasToken.isEmpty()) {
                fullUrl += "?" + sasToken;
            }

            log.info("Uploading file to Azure Blob Storage: {}", blobUrl);

            URL url = new URL(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);

            // Set authentication if using account key
            if (accountKey != null && !accountKey.isEmpty()) {
                String auth = storageAccountName + ":" + accountKey;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            }

            // Set content type
            if (request.getContentType() != null) {
                connection.setRequestProperty("Content-Type", request.getContentType());
            }

            // Set blob type
            connection.setRequestProperty("x-ms-blob-type", "BlockBlob");

            // Write file content
            connection.getOutputStream().write(request.getFileContent());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            int responseCode = connection.getResponseCode();

            if (responseCode == 200 || responseCode == 201) {
                log.info("File uploaded successfully to Azure Blob Storage");
                return FileUploadResponse.builder()
                        .success(true)
                        .fileUrl(blobUrl)
                        .fileName(request.getFileName())
                        .message("File uploaded successfully")
                        .fileSize(request.getFileContent().length)
                        .build();
            } else {
                String errorResponse = readErrorResponse(connection);
                log.error("Failed to upload file to Azure Blob Storage. Status: {}, Response: {}", responseCode, errorResponse);
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Failed to upload file. Status: " + responseCode + ", Response: " + errorResponse)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error uploading file to Azure Blob Storage", e);
            return FileUploadResponse.builder()
                    .success(false)
                    .message("Error uploading file: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public boolean validateCredentials(Map<String, Object> credentials) {
        return credentials != null
                && credentials.containsKey("storageAccountName")
                && credentials.containsKey("containerName")
                && (credentials.containsKey("sasToken") || credentials.containsKey("accountKey"));
    }

    private String readErrorResponse(HttpURLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (Exception e) {
            return "Unable to read error response";
        }
    }
}
