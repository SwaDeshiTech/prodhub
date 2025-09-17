package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.CodeFreezeRequest;
import com.swadeshitech.prodhub.dto.CodeFreezeResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.CodeFreezeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/codeFreeze")
@RequiredArgsConstructor
public class CodeFreeze {

    private CodeFreezeService codeFreezeService;

    @PostMapping
    public ResponseEntity<Response> createCodeFreeze(@RequestBody CodeFreezeRequest request) {

        CodeFreezeResponse codeFreezeResponse = codeFreezeService.createCodeFreeze(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Code freeze has been created")
                .response(codeFreezeResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Response> getCodeFreezeDetails(@PathVariable String id) {

        CodeFreezeResponse codeFreezeResponse = codeFreezeService.codeFreezeDetails(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Code freeze details has been fetched successfully")
                .response(codeFreezeResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getCodeFreezeList() {

        List<CodeFreezeResponse> codeFreezeResponse = codeFreezeService.getCodeFreezeList();

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Code freeze list has been fetched successfully")
                .response(codeFreezeResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateCodeFreeze(@PathVariable String id, @RequestBody CodeFreezeRequest request) {

        codeFreezeService.updateCodeFreeze(id, request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Code freeze has been updated successfully")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
