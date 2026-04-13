package com.swadeshitech.prodhub.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.entity.CredentialProvider;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.storage.FileUploadRequest;
import com.swadeshitech.prodhub.integration.storage.FileUploadResponse;
import com.swadeshitech.prodhub.integration.storage.StorageProvider;
import com.swadeshitech.prodhub.integration.storage.StorageProviderFactory;
import com.swadeshitech.prodhub.integration.vault.VaultApiService;
import com.swadeshitech.prodhub.services.FileUploadService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    // Allowed file extensions
    private static final List<String> ALLOWED_FILE_EXTENSIONS = Arrays.asList(
            "xls", "xlsx",    // Excel
            "pdf",           // PDF
            "doc", "docx",   // Word
            "txt",           // Text
            "json",          // JSON
            "yaml", "yml"    // YAML
    );

    @Autowired
    private StorageProviderFactory storageProviderFactory;

    @Autowired
    private VaultApiService vaultApiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Override
    public FileUploadResponse uploadFile(String metadataId, MultipartFile file, String serviceName,
                                         String fileName, String folderPath, Map<String, String> metadata) {
        try {
            // Validate inputs
            if (!StringUtils.hasText(metadataId)) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Metadata ID is required")
                        .build();
            }

            if (file == null || file.isEmpty()) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("File is required")
                        .build();
            }

            // Validate file type
            String originalFileName = file.getOriginalFilename();
            if (!isValidFileType(originalFileName)) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Invalid file type. Allowed types: " + String.join(", ", ALLOWED_FILE_EXTENSIONS))
                        .build();
            }

            // Get file content
            byte[] fileContent = file.getBytes();
            String finalFileName = StringUtils.hasText(fileName) ? fileName : originalFileName;
            String contentType = file.getContentType();

            return uploadFileContent(metadataId, finalFileName, contentType, fileContent, serviceName, folderPath, metadata);

        } catch (Exception e) {
            log.error("Error uploading file", e);
            return FileUploadResponse.builder()
                    .success(false)
                    .message("Error uploading file: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public FileUploadResponse uploadFileContent(String metadataId, String fileName, String contentType,
                                                 byte[] fileContent, String serviceName, String folderPath,
                                                 Map<String, String> metadata) {
        try {
            // Validate inputs
            if (!StringUtils.hasText(metadataId)) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Metadata ID is required")
                        .build();
            }

            if (!StringUtils.hasText(fileName)) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("File name is required")
                        .build();
            }

            if (fileContent == null || fileContent.length == 0) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("File content is required")
                        .build();
            }

            // Validate file type
            if (!isValidFileType(fileName)) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Invalid file type. Allowed types: " + String.join(", ", ALLOWED_FILE_EXTENSIONS))
                        .build();
            }

            // Build evidence path: evidence/{serviceName}/...
            String evidencePath = buildEvidencePath(serviceName, folderPath);

            // Get credential provider by metadata ID
            CredentialProvider credentialProvider = getCredentialProvider(metadataId);

            // Get storage provider based on credential provider type
            StorageProvider storageProvider = storageProviderFactory.getProvider(
                    credentialProvider.getCredentialProvider()
            );

            // Get credentials from vault
            Map<String, Object> vaultCredentials = vaultApiService.getSecret(credentialProvider.getCredentialPath());
            
            if (vaultCredentials == null) {
                log.error("Failed to retrieve credentials from vault for path: {}", credentialProvider.getCredentialPath());
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Failed to retrieve credentials from vault")
                        .build();
            }

            // Parse the secret from vault response
            Map<String, Object> credentials = parseVaultCredentials(vaultCredentials);

            // Validate credentials
            if (!storageProvider.validateCredentials(credentials)) {
                log.error("Invalid credentials for provider: {}", storageProvider.getProviderType());
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Invalid credentials for storage provider")
                        .build();
            }

            // Build upload request
            FileUploadRequest uploadRequest = FileUploadRequest.builder()
                    .fileName(fileName)
                    .contentType(contentType)
                    .fileContent(fileContent)
                    .folderPath(evidencePath)
                    .metadata(metadata)
                    .build();

            // Upload file
            FileUploadResponse response = storageProvider.uploadFile(uploadRequest, credentials);
            
            log.info("File upload completed for metadataId: {}, fileName: {}, serviceName: {}, success: {}", 
                    metadataId, fileName, serviceName, response.isSuccess());
            
            return response;

        } catch (Exception e) {
            log.error("Error uploading file content", e);
            return FileUploadResponse.builder()
                    .success(false)
                    .message("Error uploading file: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean isValidFileType(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }
        
        String extension = getFileExtension(fileName);
        return ALLOWED_FILE_EXTENSIONS.contains(extension.toLowerCase());
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    private String buildEvidencePath(String serviceName, String additionalPath) {
        StringBuilder pathBuilder = new StringBuilder("evidence");
        
        if (StringUtils.hasText(serviceName)) {
            pathBuilder.append("/").append(serviceName);
        }
        
        if (StringUtils.hasText(additionalPath)) {
            pathBuilder.append("/").append(additionalPath);
        }
        
        return pathBuilder.toString();
    }

    private CredentialProvider getCredentialProvider(String metadataId) {
        List<CredentialProvider> credentialProviders = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", new ObjectId(metadataId)),
                CredentialProvider.class
        );

        if (CollectionUtils.isEmpty(credentialProviders)) {
            log.error("Credential provider not found for metadata ID: {}", metadataId);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_NOT_FOUND);
        }

        CredentialProvider credentialProvider = credentialProviders.getFirst();
        
        if (!credentialProvider.isActive()) {
            log.error("Credential provider is not active for metadata ID: {}", metadataId);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_NOT_FOUND);
        }

        return credentialProvider;
    }

    private Map<String, Object> parseVaultCredentials(Map<String, Object> vaultResponse) {
        Map<String, Object> credentials = new HashMap<>();
        
        // Vault response typically has the actual secret under a "secret" key
        Object secretData = vaultResponse.get("secret");
        
        if (secretData instanceof String) {
            try {
                // If secret is a JSON string, parse it
                JsonNode secretNode = objectMapper.readTree((String) secretData);
                credentials = objectMapper.convertValue(secretNode, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                log.error("Failed to parse secret JSON", e);
                // If parsing fails, treat it as a simple value
                credentials.put("secret", secretData);
            }
        } else if (secretData instanceof Map) {
            // If secret is already a map
            credentials.putAll((Map<String, Object>) secretData);
        } else {
            credentials.putAll(vaultResponse);
        }
        
        return credentials;
    }
}
