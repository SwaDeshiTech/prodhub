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

import com.swadeshitech.prodhub.dto.BuildProviderResponse;
import com.swadeshitech.prodhub.dto.BuilderProviderRequest;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.BuildProviderService;

@RestController
@RequestMapping("/buildProvider")
public class BuildProvider {

    @Autowired
    private BuildProviderService buildProviderService;

    @PostMapping
    public ResponseEntity<Response> onboardBuildProvider(@RequestBody BuilderProviderRequest request) {

        BuildProviderResponse buildProviderResponse = buildProviderService.onboardBuildProvider(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully onboarded build provider")
                .response(buildProviderResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/onboarded")
    public ResponseEntity<Response> getOnboardedBuildProviders() {

        List<BuildProviderResponse> buildProviderResponses = buildProviderService.registeredBuildProviders();

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully fetched onboarded build providers")
                .response(buildProviderResponses)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Response> getRegisteredCloudProviderDetails(@PathVariable String id) {

        BuildProviderResponse buildProviderResponse = buildProviderService.getBuildProvider(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully fetched registered build provider details")
                .response(buildProviderResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<Response> getCloudProvider() {

        List<BuildProviderResponse> buildProviderResponses = buildProviderService.getAllBuildProviders();

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully fetched build providers")
                .response(buildProviderResponses)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> removeCloudProvider(@PathVariable String id) {

        String deletedResponse = buildProviderService.removeBuildProvider(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully deregistered build provider")
                .response(deletedResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

}
