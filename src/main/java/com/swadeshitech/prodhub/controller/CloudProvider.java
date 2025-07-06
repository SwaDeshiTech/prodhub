package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.CloudProviderDetailsResponse;
import com.swadeshitech.prodhub.dto.CloudProviderRegisterRequest;
import com.swadeshitech.prodhub.dto.CloudProviderRegisterResponse;
import com.swadeshitech.prodhub.dto.CloudProviderResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.CloudProviderService;

@RestController
@RequestMapping("/cloudProvider")
public class CloudProvider {

    @Autowired
    private CloudProviderService cloudProviderService;

    @GetMapping("/onboarded")
    public ResponseEntity<Response> getRegisteredCloudProviders() {

        List<CloudProviderResponse> cloudProviderResponses = cloudProviderService.registeredCloudProviderList();

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully fetched registered cloud providers")
                .response(cloudProviderResponses)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Response> getRegisteredCloudProviderDetails(@PathVariable String id) {

        CloudProviderDetailsResponse cloudProviderResponses = cloudProviderService.getCloudProviderDetails(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully fetched registered cloud provider details")
                .response(cloudProviderResponses)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Response> registerCloudProvider(@RequestBody CloudProviderRegisterRequest registerRequest) {

        CloudProviderRegisterResponse cloudProviderRegisterResponse = cloudProviderService
                .registerCloudProvider(registerRequest);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully onboarded cloud provider")
                .response(cloudProviderRegisterResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Response> getCloudProvider() {

        List<CloudProviderResponse> cloudProviderResponses = cloudProviderService.cloudProviderList();

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully fetched cloud providers")
                .response(cloudProviderResponses)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> removeCloudProvider(@PathVariable String id) {

        String deletedResponse = cloudProviderService.deleteCloudProvider(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully deregistered cloud provider")
                .response(deletedResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

}
