package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.PipelineTemplateRequest;
import com.swadeshitech.prodhub.dto.PipelineTemplateResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.PipelineTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pipelineTemplate")
public class PipelineTemplate {

    @Autowired
    PipelineTemplateService pipelineTemplateService;

    @PostMapping
    public ResponseEntity<Response> createPipelineTemplate(@RequestBody PipelineTemplateRequest pipelineTemplateRequest) {

        pipelineTemplateService.createPipelineTemplate(pipelineTemplateRequest);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully created pipeline template")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getPipelineTemplateDetails(@PathVariable String id, @RequestParam String version) {

        PipelineTemplateResponse pipelineTemplateResponse = pipelineTemplateService.getPipelineTemplateDetails(id, version);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully fetched pipeline template details")
                .response(pipelineTemplateResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/dropdown")
    public ResponseEntity<Response> getPipelineTemplatesForDropdown() {

        List<DropdownDTO> dropdownList = pipelineTemplateService.getAllPipelineTemplatesForDropdown();

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully fetched pipeline templates for dropdown")
                .response(dropdownList)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}

