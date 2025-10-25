package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.services.DeploymentSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deployment-set")
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
    public ResponseEntity<Response> getDeploymentSetDetails() {

        List<DeploymentSetResponse> deploymentSetResponses = deploymentSetService.getDeploymentResponseList();

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
}
