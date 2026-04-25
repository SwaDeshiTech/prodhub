package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.ResourceAllocationRuleRequest;
import com.swadeshitech.prodhub.dto.ResourceAllocationRuleResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.ResourceAllocationRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/resourceAllocationRule")
public class ResourceAllocationRule {

    @Autowired
    private ResourceAllocationRuleService resourceAllocationRuleService;

    @PostMapping
    public ResponseEntity<Response> createRule(@RequestBody ResourceAllocationRuleRequest request) {
        ResourceAllocationRuleResponse response = resourceAllocationRuleService.createRule(request);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Resource allocation rule created successfully")
                .response(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{ruleId}")
    public ResponseEntity<Response> getRule(@PathVariable String ruleId) {
        ResourceAllocationRuleResponse response = resourceAllocationRuleService.getRule(ruleId);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Resource allocation rule fetched successfully")
                .response(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<Response> getAllRules() {
        List<ResourceAllocationRuleResponse> response = resourceAllocationRuleService.getAllRules();
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Resource allocation rules fetched successfully")
                .response(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<Response> updateRule(@PathVariable String ruleId, @RequestBody ResourceAllocationRuleRequest request) {
        ResourceAllocationRuleResponse response = resourceAllocationRuleService.updateRule(ruleId, request);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Resource allocation rule updated successfully")
                .response(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Response> deleteRule(@PathVariable String ruleId) {
        resourceAllocationRuleService.deleteRule(ruleId);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Resource allocation rule deleted successfully")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }
}
