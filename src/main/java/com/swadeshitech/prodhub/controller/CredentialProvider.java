package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.services.CredentialProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/details/{credentialId}")
    public ResponseEntity<Response> credentialProviderDetails(@PathVariable String credentialId) {

        CredentialProviderResponse credentialProviderResponse = credentialProviderService.credentialProviderDetails(credentialId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Credential details has been fetched")
                .response(credentialProviderResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{serviceId}/{credentialId}")
    public ResponseEntity<Response> credentialProviderDetails(@PathVariable String serviceId, @PathVariable String credentialId) {

        CredentialProviderResponse credentialProviderResponse = credentialProviderService.credentialProviderDetails(serviceId, credentialId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Credential details has been fetched")
                .response(credentialProviderResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Response> filterCredentialProvider(@ModelAttribute CredentialProviderFilter credentialProviderFilter) {

        List<CredentialProviderResponse> credentialProviderResponses = credentialProviderService.credentialProviders(credentialProviderFilter);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Credential provider list has been fetched")
                .response(credentialProviderResponses)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/dropdown/{credentialType}")
    public ResponseEntity<Response> getCredentialProviders(@PathVariable String credentialType) {

        List<DropdownDTO> dropdownDTOS = credentialProviderService.getCredentialProvidersByType(credentialType);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Credential dropdown list has been fetched")
                .response(dropdownDTOS)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/details/{credentialId}")
    public ResponseEntity<Response> updateCredentialProvider(@PathVariable String credentialId, @RequestBody CredentialProviderRequest request) {

        CredentialProviderResponse credentialProviderResponse = credentialProviderService.updateCredentialProvider(credentialId, request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Credential has been updated successfully")
                .response(credentialProviderResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/sync")
    public ResponseEntity<Response> syncCredentials(@RequestBody Map<String, Object> request) {
        String scmCredentialId = (String) request.get("scmCredentialId");
        @SuppressWarnings("unchecked")
        List<String> buildProviderIds = (List<String>) request.get("buildProviderIds");

        Map<String, Object> syncResult = credentialProviderService.syncCredentials(scmCredentialId, buildProviderIds);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Credentials sync completed")
                .response(syncResult)
                .build();

        return ResponseEntity.ok().body(response);
    }
}
