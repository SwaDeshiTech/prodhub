package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.MetaDataResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.MetadataService;

@RestController
@RequestMapping("/metadata")
public class Metadata {

    @Autowired
    private MetadataService metadataService;

    @GetMapping("/dropdown")
    public ResponseEntity<Response> getMetadataForDropdown(@RequestParam(name = "applicationId") String applicationId,
            @RequestParam(name = "type") String type) {

        List<DropdownDTO> metadataList = metadataService.getAllMetadataNames(applicationId, type);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Metadata list has been fetched successfully")
                .response(metadataList)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Response> getMetadataDetailsById(@PathVariable String id) {

        MetaDataResponse metadataDetails = metadataService.getMetadataDetails(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Metadata details has been fetched successfully")
                .response(metadataDetails)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/all/{applicationId}")
    public ResponseEntity<Response> getAllMetadataByApplicationId(@PathVariable String applicationId) {

        List<MetaDataResponse> allMetadata = metadataService.getAllMetadataDetails(applicationId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("All metadata has been fetched successfully")
                .response(allMetadata)
                .build();

        return ResponseEntity.ok().body(response);
    }
}
