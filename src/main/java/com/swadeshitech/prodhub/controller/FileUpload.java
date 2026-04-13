package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.integration.storage.FileUploadResponse;
import com.swadeshitech.prodhub.services.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/file-upload")
@Slf4j
@Tag(name = "File Upload", description = "API for uploading files to various storage providers")
public class FileUpload {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file to storage provider", description = "Uploads a file to the specified storage provider using credential provider metadata ID")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "Credential provider metadata ID", required = true)
            @RequestParam("metadataId") String metadataId,
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Service name for folder structure (evidence/{serviceName}/...)")
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @Parameter(description = "Custom file name (if not provided, uses original file name)")
            @RequestParam(value = "fileName", required = false) String fileName,
            @Parameter(description = "Optional additional folder path in storage")
            @RequestParam(value = "folderPath", required = false) String folderPath,
            @Parameter(description = "Optional metadata as JSON string")
            @RequestParam(value = "metadata", required = false) String metadataJson
    ) {
        log.info("Received file upload request for metadataId: {}, fileName: {}, serviceName: {}", 
                metadataId, file.getOriginalFilename(), serviceName);

        try {
            Map<String, String> metadata = null;
            // Parse metadata if provided
            if (metadataJson != null && !metadataJson.isEmpty()) {
                // You can use ObjectMapper to parse JSON if needed
                // For simplicity, we'll keep it as is for now
            }

            FileUploadResponse response = fileUploadService.uploadFile(
                    metadataId, file, serviceName, fileName, folderPath, metadata
            );

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error processing file upload request", e);
            FileUploadResponse errorResponse = FileUploadResponse.builder()
                    .success(false)
                    .message("Error processing upload: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/upload-content")
    @Operation(summary = "Upload file content directly", description = "Uploads file content directly to the specified storage provider")
    public ResponseEntity<FileUploadResponse> uploadFileContent(
            @Parameter(description = "Credential provider metadata ID", required = true)
            @RequestParam("metadataId") String metadataId,
            @Parameter(description = "File name", required = true)
            @RequestParam("fileName") String fileName,
            @Parameter(description = "Content type", required = true)
            @RequestParam("contentType") String contentType,
            @Parameter(description = "File content as base64 encoded string", required = true)
            @RequestParam("fileContent") String fileContentBase64,
            @Parameter(description = "Service name for folder structure (evidence/{serviceName}/...)")
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @Parameter(description = "Optional additional folder path in storage")
            @RequestParam(value = "folderPath", required = false) String folderPath,
            @Parameter(description = "Optional metadata as JSON string")
            @RequestParam(value = "metadata", required = false) String metadataJson
    ) {
        log.info("Received file content upload request for metadataId: {}, fileName: {}, serviceName: {}", 
                metadataId, fileName, serviceName);

        try {
            // Decode base64 content
            byte[] fileContent = java.util.Base64.getDecoder().decode(fileContentBase64);

            Map<String, String> metadata = null;
            // Parse metadata if provided
            if (metadataJson != null && !metadataJson.isEmpty()) {
                // You can use ObjectMapper to parse JSON if needed
            }

            FileUploadResponse response = fileUploadService.uploadFileContent(
                    metadataId, fileName, contentType, fileContent, serviceName, folderPath, metadata
            );

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error processing file content upload request", e);
            FileUploadResponse errorResponse = FileUploadResponse.builder()
                    .success(false)
                    .message("Error processing upload: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
