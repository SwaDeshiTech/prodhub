package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.PipelineExecutionDetailsDTO;
import com.swadeshitech.prodhub.dto.PipelineExecutionRequest;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.PipelineService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pipeline")
public class Pipeline {

    @Autowired
    PipelineService pipelineService;

    @PostMapping
    @RequestMapping("/startExecution")
    public ResponseEntity<Response> startPipelineExecution(@RequestBody PipelineExecutionRequest request) {

        String pipelineExecutionId = pipelineService.schedulePipelineExecution(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully scheduled pipeline")
                .response(pipelineExecutionId)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @RequestMapping("/execution/{pipelineExecutionId}")
    public ResponseEntity<Response> getPipelineExecutionStatus(@PathVariable String pipelineExecutionId) {

        PipelineExecutionDetailsDTO pipelineExecutionDetails = pipelineService
                .getPipelineExecutionDetails(pipelineExecutionId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully retrieved pipeline execution details")
                .response(pipelineExecutionDetails)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @RequestMapping("/executions")
    public ResponseEntity<Response> getPipelineExecutions(@RequestParam Map<String, Object> filters) {

        if (!filters.containsKey("serviceId")) {
            throw new CustomException(ErrorCode.PIPELINE_SERVICE_ID_IS_MANDATORY);
        }

        List<PipelineExecutionDetailsDTO> pipelineExecutions = pipelineService
                .getPipelineExecutions(filters);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully retrieved pipeline executions")
                .response(pipelineExecutions)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
