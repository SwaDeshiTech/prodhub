package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.DeploymentTemplateRequest;
import com.swadeshitech.prodhub.dto.DeploymentTemplateResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.DeploymentTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deploymentTemplate")
public class DeploymentTemplate {

    @Autowired
    DeploymentTemplateService deploymentTemplateService;

    @PostMapping
    public ResponseEntity<Response> createDeploymentTemplate(@RequestBody DeploymentTemplateRequest request) {

        DeploymentTemplateResponse deploymentTemplateResponse = deploymentTemplateService.createDeploymentTemplate(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Deployment template has been onboarded")
                .response(deploymentTemplateResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
