package com.swadeshitech.prodhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.UserRequest;
import com.swadeshitech.prodhub.dto.UserResponse;
import com.swadeshitech.prodhub.services.UserService;

@RestController
@RequestMapping("/user")
public class User {

    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<Response> user(@RequestHeader(name = "uuid") String uuid) {
        
        UserResponse userResponse = userService.getUserDetail(uuid);
        
        Response response = Response.builder()
            .httpStatus(HttpStatus.OK)
            .message("User Detail has been fetched successfully")
            .response(userResponse)
            .build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Response> user(@RequestBody UserRequest userRequest) {
        
        UserResponse userResponse = userService.addUser(userRequest);

        Response response = Response.builder()
            .httpStatus(HttpStatus.CREATED)
            .message("User has been created")
            .response(userResponse)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
