package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.PipelineExecutionRequest;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Pipeline {

    @Autowired
    PipelineService pipelineService;

    @PostMapping
    public ResponseEntity<Response> startPipelineExecution(@RequestBody PipelineExecutionRequest request) {

        String pipelineExecutionId = pipelineService.schedulePipelineExecution(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully scheduled pipeline")
                .response(pipelineExecutionId)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
