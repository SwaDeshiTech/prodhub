package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @PostMapping("/internal/login")
    public ResponseEntity<Response> internalLogin(@RequestBody Map<String, String> credentials) {
        Response response = authService.internalLogin(credentials);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @GetMapping("/check-username-availability")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam String username) {
        boolean available = userRepository.findByUserName(username).isEmpty();
        return ResponseEntity.ok(Map.of("available", available));
    }

    @PostMapping("/internal/signup")
    public ResponseEntity<Response> internalSignup(@RequestBody Map<String, String> userData) {
        Response response = authService.internalSignup(userData);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
}