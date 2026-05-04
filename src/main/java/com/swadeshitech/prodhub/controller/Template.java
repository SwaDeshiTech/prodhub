package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.TemplateRequest;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.TemplateResponse;
import com.swadeshitech.prodhub.services.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/template")
public class Template {

    @Autowired
    TemplateService templateService;

    @PostMapping
    public ResponseEntity<Response> createTemplate(@RequestBody TemplateRequest request) {

        TemplateResponse templateResponse = templateService.createTemplate(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Template has been onboarded")
                .response(templateResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Response> getAllTemplates() {

        List<TemplateResponse> templates = templateService.getAllTemplates();

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully fetched all templates")
                .response(templates)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getTemplateDetails(@PathVariable String id) {

        TemplateResponse template = templateService.getTemplateDetails(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully fetched template details")
                .response(template)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
