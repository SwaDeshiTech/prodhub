package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.DeploymentSetRequest;
import com.swadeshitech.prodhub.dto.DeploymentSetResponse;
import com.swadeshitech.prodhub.dto.DeploymentSetUpdateRequest;
import com.swadeshitech.prodhub.dto.PaginatedResponse;
import com.swadeshitech.prodhub.enums.DeploymentSetStatus;
import com.swadeshitech.prodhub.integration.storage.FileUploadResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public interface DeploymentSetService {

    String createDeploymentSet(DeploymentSetRequest request);

    DeploymentSetResponse getDeploymentSetDetails(String id);

    PaginatedResponse<DeploymentSetResponse> getDeploymentResponseList(Integer page, Integer size, String sortBy, String order);

    void updateDeploymentSet(String id, DeploymentSetUpdateRequest request);

    void updateDeploymentSetStatus(String deploymentSetIDUUID);

    /**
     * Uploads evidence file for a deployment set
     * 
     * @param deploymentSetId The deployment set ID
     * @param file The file to upload
     * @return FileUploadResponse with upload result
     */
    FileUploadResponse uploadEvidenceFile(String deploymentSetId, MultipartFile file);
}
