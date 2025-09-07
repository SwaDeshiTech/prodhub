package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.CredentialProviderRequest;
import com.swadeshitech.prodhub.dto.CredentialProviderResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.CredentialProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/credentialProvider")
public class CredentialProvider {

    @Autowired
    private CredentialProviderService credentialProviderService;

    @PostMapping
    public ResponseEntity<Response> onboardCredential(@RequestBody CredentialProviderRequest request) {

        CredentialProviderResponse credentialProviderResponse = credentialProviderService.onboardCredentialProvider(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Credential has been onboarded")
                .response(credentialProviderResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{serviceId}/{credentialId}")
    public ResponseEntity<Response> credentialProviderDetails(String serviceId, String credentialId) {

        CredentialProviderResponse credentialProviderResponse = credentialProviderService.credentialProviderDetails(serviceId, credentialId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Credential details has been fetched")
                .response(credentialProviderResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }
}
