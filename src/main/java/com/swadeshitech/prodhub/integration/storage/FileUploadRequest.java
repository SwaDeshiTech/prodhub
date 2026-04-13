package com.swadeshitech.prodhub.integration.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadRequest {
    private String fileName;
    private String contentType;
    private byte[] fileContent;
    private String folderPath;
    private Map<String, String> metadata;
}
