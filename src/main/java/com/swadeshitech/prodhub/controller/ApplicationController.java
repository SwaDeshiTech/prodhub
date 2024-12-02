package com.swadeshitech.prodhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.ApplicationRequest;
import com.swadeshitech.prodhub.dto.ApplicationResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.ApplicationService;

@RestController
@RequestMapping("/application")
public class ApplicationController {
    
    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Response> application(@RequestBody ApplicationRequest applicationRequest) {

        ApplicationResponse applicationResponse = applicationService.addApplication(applicationRequest);

        Response response = Response.builder()
            .httpStatus(HttpStatus.CREATED)
            .message("Application has been onboarded")
            .response(applicationResponse)
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> application(@PathVariable("id") String id) {

        ApplicationResponse applicationResponse = applicationService.getApplicationDetail(id);

        Response response = Response.builder()
            .httpStatus(HttpStatus.OK)
            .message("Application has been fetched successfully")
            .response(applicationResponse)
            .build();
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
