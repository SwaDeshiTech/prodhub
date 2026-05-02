package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.TabRequest;
import com.swadeshitech.prodhub.dto.TabResponse;
import com.swadeshitech.prodhub.services.TabService;

@RestController
@RequestMapping("/tab")
public class Tab {

    @Autowired
    private TabService tabService;

    @GetMapping
    public ResponseEntity<Response> getActiveTabsByUser() {

        List<TabResponse> tabs = tabService.getActiveTabsByUser();

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

    @PutMapping("/{id}")
    public ResponseEntity<Response> updateTab(@PathVariable String id, @RequestBody TabRequest tabRequest) {

        TabResponse tabResponse = tabService.updateTab(id, tabRequest);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully updated the tab")
                .response(tabResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteTab(@PathVariable String id) {

        tabService.deleteTab(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully deleted the tab")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
