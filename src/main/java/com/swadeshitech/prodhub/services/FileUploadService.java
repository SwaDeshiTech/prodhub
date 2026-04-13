package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.integration.storage.FileUploadRequest;
import com.swadeshitech.prodhub.integration.storage.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileUploadService {
    
    /**
     * Uploads a file to the specified storage provider using the credential provider metadata ID
     * 
     * @param metadataId The credential provider metadata ID
     * @param file The file to upload
     * @param serviceName The service name for folder structure (evidence/{serviceName}/...)
     * @param fileName The custom file name (if null, will use original file name)
     * @param folderPath Optional additional folder path in the storage
     * @param metadata Optional metadata to attach to the file
     * @return FileUploadResponse with upload result
     */
    FileUploadResponse uploadFile(String metadataId, MultipartFile file, String serviceName, 
                                   String fileName, String folderPath, Map<String, String> metadata);
    
    /**
     * Uploads file content directly to the specified storage provider
     * 
     * @param metadataId The credential provider metadata ID
     * @param fileName The name of the file
     * @param contentType The content type of the file
     * @param fileContent The byte content of the file
     * @param serviceName The service name for folder structure (evidence/{serviceName}/...)
     * @param folderPath Optional additional folder path in the storage
     * @param metadata Optional metadata to attach to the file
     * @return FileUploadResponse with upload result
     */
    FileUploadResponse uploadFileContent(String metadataId, String fileName, String contentType, 
                                         byte[] fileContent, String serviceName, String folderPath, 
                                         Map<String, String> metadata);
    
    /**
     * Validates if the file type is allowed
     * 
     * @param fileName The file name to validate
     * @return true if file type is allowed, false otherwise
     */
    boolean isValidFileType(String fileName);
}
