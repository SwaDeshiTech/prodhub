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
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.SCMDetailsResponse;
import com.swadeshitech.prodhub.dto.SCMRegisterRequest;
import com.swadeshitech.prodhub.dto.SCMRegisterResponse;
import com.swadeshitech.prodhub.dto.SCMResponse;
import com.swadeshitech.prodhub.services.SCMService;

@RestController
@RequestMapping("/scm")
public class SCM {

    @Autowired
    private SCMService scmService;

    @GetMapping("/onboarded")
    public ResponseEntity<Response> getRegisteredSCMList() {

        List<SCMResponse> scmResponses = scmService.registeredSCMList();

        Response response = Response.builder().httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully fetched registered scms").response(scmResponses).build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Response> getSCMDetails(@PathVariable String id) {

        SCMDetailsResponse scmDetailsResponse = scmService.getSCMDetails(id);

        Response response = Response.builder().httpStatus(HttpStatus.ACCEPTED)
                .message("Successfully fetched registered scm details").response(scmDetailsResponse).build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Response> registerSCM(@RequestBody SCMRegisterRequest registerRequest) {

        SCMRegisterResponse scmResponse = scmService.registerSCM(registerRequest);

        Response response = Response.builder().httpStatus(HttpStatus.CREATED).message("Successfully onboarded scm")
                .response(scmResponse).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Response> getSCMList() {

        List<SCMResponse> scmResponses = scmService.scmList();

        Response response = Response.builder().httpStatus(HttpStatus.ACCEPTED).message("Successfully fetched scm list")
                .response(scmResponses).build();

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteSCM(@PathVariable String id) {

        String deletedResponse = scmService.deleteSCM(id);

        Response response = Response.builder().httpStatus(HttpStatus.ACCEPTED).message("Successfully deregistered scm")
                .response(deletedResponse).build();

        return ResponseEntity.ok().body(response);
    }
}
