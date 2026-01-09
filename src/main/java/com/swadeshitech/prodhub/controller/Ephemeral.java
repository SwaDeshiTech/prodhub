package com.swadeshitech.prodhub.controller;

import java.util.List;

import com.swadeshitech.prodhub.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swadeshitech.prodhub.services.EphemeralEnvironmentService;

@RestController
@RequestMapping("/ephemeralEnvironment")
public class Ephemeral {

    @Autowired
    EphemeralEnvironmentService environmentService;

    @GetMapping("/dropdown")
    public ResponseEntity<Response> ephemeralEnvironmentDropdownList() {

        List<DropdownDTO> environmentResponse = environmentService.getEphemeralEnvironmentDropdownList();

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Ephemeral Environments has been fetched successfully")
                .response(environmentResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<Response> ephemeralEnvironmentList(@RequestParam(defaultValue = "0") Integer page,
                                                             @RequestParam(defaultValue = "10") Integer size,
                                                             @RequestParam(defaultValue = "createdTime") String sortBy,
                                                             @RequestParam(defaultValue = "DESC") String order) {

        PaginatedResponse<EphemeralEnvironmentResponse> environmentResponse = environmentService.getEphemeralEnvironmentList(page, size, sortBy, order);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Ephemeral Environment List has been fetched successfully")
                .response(environmentResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> ephemeralEnvironmentDetail(@PathVariable("id") String id) {

        EphemeralEnvironmentResponse environmentResponse = environmentService.getEphemeralEnvironmentDetail(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Ephemeral Environment has been fetched successfully")
                .response(environmentResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}/{applicationId}")
    public ResponseEntity<Response> ephemeralEnvironmentApplicationDetails(@PathVariable("id") String id,
            @PathVariable("applicationId") String applicationId) {

        EphemeralEnvironmentApplicationResponse environmentResponse = environmentService
                .getEphemeralEnvironmentApplicationDetails(id, applicationId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Ephemeral Environment Service has been fetched successfully")
                .response(environmentResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Response> ephemeralEnvironmentDetail(@RequestBody EphemeralEnvironmentRequest request) {

        EphemeralEnvironmentResponse environmentResponse = environmentService.createEphemeralEnvironment(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Ephemeral Environment has been created successfully")
                .response(environmentResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateEphemeralEnvironmentDetail(@PathVariable String id,
            @RequestBody EphemeralEnvironmentRequest request) {

        EphemeralEnvironmentResponse environmentResponse = environmentService.updateEphemeralEnvironment(id, request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Ephemeral Environment has been updated successfully")
                .response(environmentResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/buildAndDeploy")
    public ResponseEntity<Response> buildAndDeploy(@RequestBody EphemeralEnvironmentBuildAndDeployRequest request) {

        String message = environmentService.buildAndDeployment(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Build and deployment in Ephemeral Environment has been triggered successfully")
                .response(message)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
