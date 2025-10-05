package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swadeshitech.prodhub.config.ContextHolder;
import com.swadeshitech.prodhub.dto.ReleaseCandidateRequest;
import com.swadeshitech.prodhub.dto.ReleaseCandidateResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.ReleaseCandidateService;

@RestController
@RequestMapping("/release-candidate")
public class ReleaseCandidate {

    @Autowired
    private ReleaseCandidateService releaseCandidateService;

    @PostMapping
    public ResponseEntity<Response> createReleaseCandidate(@RequestHeader(name = "uuid") String uuid,
            @RequestBody ReleaseCandidateRequest request) {

        try {
            ContextHolder.setContext("uuid", uuid);
            ReleaseCandidateResponse releaseCandidateResponse = releaseCandidateService.createReleaseCandidate(request);

            Response response = Response.builder()
                    .httpStatus(HttpStatus.CREATED)
                    .message("Successfully created release candidate")
                    .response(releaseCandidateResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } finally {
            // Clear the context to avoid memory leaks
            ContextHolder.clearContext();
        }
    }

    @PutMapping("/syncStatus/{id}")
    public ResponseEntity<Response> syncStatus(@PathVariable String id, @RequestParam String forceSync) {

        ReleaseCandidateResponse releaseCandidateResponse = releaseCandidateService.syncStatus(id, forceSync);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully synced build status")
                .response(releaseCandidateResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateReleaseCandidate(@RequestHeader(name = "uuid") String uuid,
            @RequestBody ReleaseCandidateRequest request, String id) {

        try {
            ContextHolder.setContext("uuid", uuid);
            ReleaseCandidateResponse releaseCandidateResponse = releaseCandidateService.updateReleaseCandidate(id,
                    request);

            Response response = Response.builder()
                    .httpStatus(HttpStatus.OK)
                    .message("Successfully updated release candidate")
                    .response(releaseCandidateResponse)
                    .build();

            return ResponseEntity.ok(response);
        } finally {
            // Clear the context to avoid memory leaks
            ContextHolder.clearContext();
        }
    }

    @GetMapping
    public ResponseEntity<Response> getAllReleaseCandidates(@RequestHeader(name = "uuid") String uuid) {

        try {
            ContextHolder.setContext("uuid", uuid);
            List<ReleaseCandidateResponse> releaseCandidateResponse = releaseCandidateService.getAllReleaseCandidates();

            Response response = Response.builder()
                    .httpStatus(HttpStatus.OK)
                    .message("Successfully retrieved all release candidates")
                    .response(releaseCandidateResponse)
                    .build();

            return ResponseEntity.ok(response);
        } finally {
            // Clear the context to avoid memory leaks
            ContextHolder.clearContext();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getReleaseCandidateById(String id) {
        ReleaseCandidateResponse releaseCandidateResponse = releaseCandidateService.getReleaseCandidateById(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully retrieved release candidate")
                .response(releaseCandidateResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteReleaseCandidate(String id) {
        releaseCandidateService.deleteReleaseCandidate(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.NO_CONTENT)
                .message("Successfully deleted release candidate")
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
