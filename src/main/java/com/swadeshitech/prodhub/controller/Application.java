package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swadeshitech.prodhub.dto.ApplicationRequest;
import com.swadeshitech.prodhub.dto.ApplicationResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.ApplicationService;

@RestController
@RequestMapping("/application")
public class Application {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/dropdown")
    public ResponseEntity<Response> getAllDepartmentsForDropdown() {

        List<DropdownDTO> applications = applicationService.getAllApplicationsDropdown();

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Applications list has been fetched successfully")
                .response(applications)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/dropdown/userAccess")
    public ResponseEntity<Response> getApplicationDropdownByUserAccess() {

        List<DropdownDTO> applications = applicationService.getApplicationDropdownByUserAccess();

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Applications list has been fetched successfully for dropdown")
                .response(applications)
                .build();

        return ResponseEntity.ok().body(response);
    }

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
