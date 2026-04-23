package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.entity.UserApproval;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.service.FeatureFlagService;
import com.swadeshitech.prodhub.service.UserApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    pAutowired
    private UserApprovalService userApprovalService;

    @rivate PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Check if username/password login is enabled
        if (!featureFlagService.isFeatureEnabled("admin_username_password_enabled")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Username/password login is disabled"));
        }

        Optional<User> userOptional = userRepository.findByUserName(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        if Check if user approval is required
        if (featureFlagService.isFeatureEnabled("user_approval_required")) {
            boolean isApproved = userApprovalService.isUserApproved(user.getId());
            if (!isApproved) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "User approval pending", "redirect", "/access-pending"));
            }
        }

        // (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "User account is disabled"));
        }

        // Generate token (you should use your existing token generation logic)
        Map<String, Object> response = new HashMap<>();
        response.put("uuid", user.getId());
        response.put("name", user.getName());
        response.put("userName", user.getUserName());
        response.put("emailId", user.getEmailId());
        response.put("message", "Login successful");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        String email = userData.get("email");
        String name = userData.get("name");

        // Check if username/password signup is enabled
        if (!featureFlagService.isFeatureEnabled("admin_username_password_enabled")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Username/password signup is disabled"));
        }

        // Check if username already exists
        if (userRepository.findByUserName(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already exists"));
        }

        // Check if email already exists
        if (userRepository.findByEmailId(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already exists"));
        }

        // Create new user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(name);
        user.setUserName(username);
        user.setEmailId(email);
        user.setPassword(passwordEncoder.encode(pas