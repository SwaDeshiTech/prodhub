package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.services.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approvals")
public class Approval {

    @Autowired
    private ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<Response> createApprovalRequest(@RequestBody ApprovalRequest approvalRequest) {

        ApprovalResponse approvalResponse = approvalService.createApprovalRequest(approvalRequest);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Approval request has been submitted")
                .response(approvalResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Response> getApprovalRequestDetails(@PathVariable String requestId) {

        ApprovalResponse approvalResponse = approvalService.getApprovalById(requestId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Approval request details has been fetched successfully")
                .response(approvalResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateApprovalRequestStatus(@PathVariable String id, @RequestBody ApprovalUpdateRequest request) {

        boolean isUpdated = approvalService.updateApprovalStatus(id, request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Approval request has been processed successfully")
                .response(isUpdated)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllApprovalRequest(@ModelAttribute ApprovalRequestFilter filter) {

        List<ApprovalResponse> approvalResponseList = approvalService.getApprovalsList(filter);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Approval request list has been fetched successfully")
                .response(approvalResponseList)
                .build();

        return ResponseEntity.ok().body(response);
    }

}
