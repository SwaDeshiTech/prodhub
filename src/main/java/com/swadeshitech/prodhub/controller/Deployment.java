package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.DeploymentRequestResponse;
import com.swadeshitech.prodhub.dto.DeploymentResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.integration.deplorch.DeploymentPodResponse;
import com.swadeshitech.prodhub.services.DeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deployment")
public class Deployment {

    @Autowired
    DeploymentService deploymentService;

    @PostMapping("/{deploymentSetId}")
    public ResponseEntity<Response> triggerDeployment(@PathVariable String deploymentSetId) {

        DeploymentRequestResponse deploymentRequestResponse = deploymentService.triggerDeployment(deploymentSetId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Deployment has been triggered successfully")
                .response(deploymentRequestResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{deploymentId}")
    public ResponseEntity<Response> deploymentDetails(@PathVariable String deploymentId) {

        DeploymentResponse deploymentResponse = deploymentService.getDeploymentDetails(deploymentId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Deployment details has been fetched successfully")
                .response(deploymentResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/podDetails/{deploymentId}")
    public ResponseEntity<Response> deploymentPodDetails(@PathVariable String deploymentId) {

        DeploymentPodResponse deploymentPodResponse = deploymentService.getDeployedPodDetails(deploymentId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Deployed pod details has been fetched successfully")
                .response(deploymentPodResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }
}
