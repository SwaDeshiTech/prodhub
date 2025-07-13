package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.TabRequest;
import com.swadeshitech.prodhub.dto.TabResponse;
import com.swadeshitech.prodhub.services.TabService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/tab")
public class Tab {

    @Autowired
    private TabService tabService;

    @GetMapping
    public ResponseEntity<Response> getActiveTabsByUser(@RequestHeader(name = "uuid") String uuid) {

        List<TabResponse> tabs = tabService.getActiveTabsByUser(uuid);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully fetched the tabs")
                .response(tabs)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<Response> tab(@RequestBody TabRequest tabRequest) {

        TabResponse tabResponse = tabService.addTab(tabRequest);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully added the tab")
                .response(tabResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
