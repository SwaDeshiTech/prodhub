package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.TemplateRequest;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.TemplateResponse;
import com.swadeshitech.prodhub.services.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
