package com.swadeshitech.prodhub.integration.storage;

import java.util.Map;

/**
 * Interface for storage providers with extensibility to support multiple storage backends
 * like JFrog Artifactory, Azure Blob Storage, AWS S3, Google Cloud Storage, etc.
 */
public interface StorageProvider {
    
    /**
     * Uploads a file to the storage provider
     * 
     * @param request The file upload request containing file content and metadata
     * @param credentials The credentials retrieved from vault
     * @return FileUploadResponse with the URL and upload status
     */
    FileUploadResponse uploadFile(FileUploadRequest request, Map<String, Object> credentials);
    
    /**
     * Gets the provider type identifier
     * 
     * @return The provider type (e.g., "JFROG_ARTIFACTORY", "BLOB_STORAGE", "AWS_S3", "GCS")
     */
    String getProviderType();
    
    /**
     * Validates if the credentials are sufficient for this provider
     * 
     * @param credentials The credentials to validate
     * @return true if credentials are valid, false otherwise
     */
    boolean validateCredentials(Map<String, Object> credentials);
}
