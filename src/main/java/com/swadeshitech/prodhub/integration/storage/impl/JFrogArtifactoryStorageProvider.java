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
 * JFrog Artifactory storage provider implementation
 */
@Component
@Slf4j
public class JFrogArtifactoryStorageProvider implements StorageProvider {

    private static final String PROVIDER_TYPE = "JFROG_ARTIFACTORY";

    @Override
    public FileUploadResponse uploadFile(FileUploadRequest request, Map<String, Object> credentials) {
        try {
            String artifactoryUrl = (String) credentials.get("artifactoryUrl");
            String username = (String) credentials.get("username");
            String password = (String) credentials.get("password");
            String repository = (String) credentials.get("repository");

            if (artifactoryUrl == null || username == null || password == null || repository == null) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Missing required credentials: artifactoryUrl, username, password, or repository")
                        .build();
            }

            // Construct the full URL for upload
            String folderPath = request.getFolderPath() != null ? request.getFolderPath() : "";
            String uploadUrl = artifactoryUrl + "/" + repository + "/" + folderPath + "/" + request.getFileName();

            log.info("Uploading file to JFrog Artifactory: {}", uploadUrl);

            URL url = new URL(uploadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);

            // Set authentication header
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Set content type
            if (request.getContentType() != null) {
                connection.setRequestProperty("Content-Type", request.getContentType());
            }

            // Write file content
            connection.getOutputStream().write(request.getFileContent());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            int responseCode = connection.getResponseCode();

            if (responseCode == 200 || responseCode == 201) {
                log.info("File uploaded successfully to JFrog Artifactory");
                return FileUploadResponse.builder()
                        .success(true)
                        .fileUrl(uploadUrl)
                        .fileName(request.getFileName())
                        .message("File uploaded successfully")
                        .fileSize(request.getFileContent().length)
                        .build();
            } else {
                String errorResponse = readErrorResponse(connection);
                log.error("Failed to upload file to JFrog Artifactory. Status: {}, Response: {}", responseCode, errorResponse);
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Failed to upload file. Status: " + responseCode + ", Response: " + errorResponse)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error uploading file to JFrog Artifactory", e);
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
                && credentials.containsKey("artifactoryUrl")
                && credentials.containsKey("username")
                && credentials.containsKey("password")
                && credentials.containsKey("repository");
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
