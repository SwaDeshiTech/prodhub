package com.swadeshitech.prodhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.EphemeralEnvironmentRequest;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.EphemeralEnvironmentService;

@RestController
@RequestMapping("/ephemeralEnvironment")
public class Ephemeral {
    
    @Autowired
    private EphemeralEnvironmentService environmentService;


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

    @PostMapping
    public ResponseEntity<Response> ephemeralEnvironmentDetail(@RequestBody EphemeralEnvironmentRequest request ) {

        EphemeralEnvironmentResponse environmentResponse = environmentService.createEphemeralEnvironment(request);

        Response response = Response.builder()
            .httpStatus(HttpStatus.ACCEPTED)
            .message("Ephemeral Environment has been fetched successfully")
            .response(environmentResponse)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
