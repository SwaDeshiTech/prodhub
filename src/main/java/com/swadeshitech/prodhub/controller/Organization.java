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
import com.swadeshitech.prodhub.dto.OrganizationRegisterRequest;
import com.swadeshitech.prodhub.dto.OrganizationRegisterResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.OrganizationService;

@RestController
@RequestMapping("/organization")
public class Organization {

    @Autowired
    private OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<Response> registerOrganization(@RequestBody OrganizationRegisterRequest request) {

        OrganizationRegisterResponse organizationRegisterResponse = organizationService.registerOrganization(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully created organization")
                .response(organizationRegisterResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Response> getOrganizationList() {

        List<DropdownDTO> organizationList = organizationService.getOrganizationList();

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully fetched organization list")
                .response(organizationList)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Response> getOrganizationDetails(@PathVariable String id) {

        OrganizationRegisterResponse organizationDetails = organizationService.getOrganizationDetails(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully fetched organization details")
                .response(organizationDetails)
                .build();

        return ResponseEntity.ok(response);
    }
}
