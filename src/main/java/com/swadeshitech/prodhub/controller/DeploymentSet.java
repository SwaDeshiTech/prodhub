package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.integration.storage.FileUploadResponse;
import com.swadeshitech.prodhub.services.DeploymentSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/deployment-set")
@Tag(name = "Deployment Set", description = "API for managing deployment sets")
public class DeploymentSet {

    @Autowired
    DeploymentSetService deploymentSetService;

    @PostMapping
    public ResponseEntity<Response> createDeploymentSet(@RequestBody DeploymentSetRequest request) {

        String id = deploymentSetService.createDeploymentSet(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Deployment set has been created")
                .response(id)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Response> getDeploymentSetList(@RequestParam(defaultValue = "0") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size,
                                                         @RequestParam(defaultValue = "createdTime") String sortBy,
                                                         @RequestParam(defaultValue = "DESC") String order) {

        PaginatedResponse<DeploymentSetResponse> deploymentSetResponses = deploymentSetService.getDeploymentResponseList(page, size, sortBy, order);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Deployment set list has been fetched successfully")
                .response(deploymentSetResponses)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getDeploymentSetDetails(@PathVariable String id) {

        DeploymentSetResponse deploymentSetResponse = deploymentSetService.getDeploymentSetDetails(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Deployment set details has been fetched successfully")
                .response(deploymentSetResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateApprovalRequestStatus(@PathVariable String id, @RequestBody DeploymentSetUpdateRequest request) {

        deploymentSetService.updateDeploymentSet(id, request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Deployment request has been processed successfully")
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/{id}/upload-evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload evidence file for deployment set", description = "Uploads evidence file to storage provider for a deployment set")
    public ResponseEntity<FileUploadResponse> uploadEvidence(
            @Parameter(description = "Deployment set ID", required = true)
            @PathVariable("id") String deploymentSetId,
            @Parameter(description = "Evidence file to upload", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        FileUploadResponse response = deploymentSetService.uploadEvidenceFile(deploymentSetId, file);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
