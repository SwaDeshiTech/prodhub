package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.ConstantsService;

@RestController
@RequestMapping("/constants")
public class Constants {

    @Autowired
    ConstantsService constantsService;

    @GetMapping("/{type}")
    public ResponseEntity<Response> getConstants(@PathVariable String type) {

        List<DropdownDTO> dropdownDTOs = constantsService.getConstants(type);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Dropdown details has been fetched successfully")
                .response(dropdownDTOs)
                .build();

        return ResponseEntity.ok().body(response);
    }
}
