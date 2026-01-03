package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentApplicationResponse;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentRequest;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.EphemeralEnvironmentService;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/ephemeralEnvironment")
public class Ephemeral {

    @Autowired
    private EphemeralEnvironmentService environmentService;

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
    public ResponseEntity<Response> ephemeralEnvironmentList() {

        List<EphemeralEnvironmentResponse> environmentResponse = environmentService.getEphemeralEnvironmentList();

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
        System.out.println(request);
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
}
