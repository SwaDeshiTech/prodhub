package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swadeshitech.prodhub.dto.ResourceDetailsRegisterRequest;
import com.swadeshitech.prodhub.dto.ResourceDetailsRegisterResponse;
import com.swadeshitech.prodhub.dto.ResourceDetailsResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.ResourceService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Controller
@RequestMapping("/resource")
public class Resource {

    @Autowired
    private ResourceService resourceService;

    @PostMapping
    public ResponseEntity<Response> registerResource(@RequestBody ResourceDetailsRegisterRequest registerRequest) {

        ResourceDetailsRegisterResponse registerResponse = resourceService.registerResource(registerRequest);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully onboarded resource")
                .response(registerResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/list/provider/:provider")
    public ResponseEntity<Response> getResourceList(@PathVariable String provider) {

        List<ResourceDetailsResponse> resourceDetailsList = resourceService.getResourceListByProvider(provider);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully fetched resource list")
                .response(resourceDetailsList)
                .build();

        return ResponseEntity.ok().body(response);
    }
}
