package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.UserOrganizationRequest;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.entity.UserApproval;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.service.UserApprovalService;
import com.swadeshitech.prodhub.services.UserOrganizationService;
import com.swadeshitech.prodhub.dto.UserOrganizationResponse;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user-approvals")
@Slf4j
public class UserApprovalController {

    @Autowired
    private UserApprovalService userApprovalService;

    @Autowired
    private UserOrganizationService userOrganizationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<UserApproval> createUserApproval(@RequestBody UserApproval userApproval) {
        UserApproval created = userApprovalService.createUserApproval(userApproval);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<UserApproval> approveUser(@PathVariable String userId) {
        String approvedBy = UserContextUtil.getUserIdFromRequestContext();
        UserApproval approval = userApprovalService.approveUser(userId, approvedBy);

        // Automatically add user to the admin's organization
        try {
            var adminOrgs = userOrganizationService.getOrganizationsForUser(approvedBy);
            if (adminOrgs != null && !adminOrgs.isEmpty()) {
                String orgId = adminOrgs.get(0).getOrganizationId();
                UserOrganizationRequest request = UserOrganizationRequest.builder()
                        .userId(approval.getUserEmail()) // Service expects email here
                        .organizationId(orgId)
                        .build();
                userOrganizationService.addUserToOrganization(request);
            }
        } catch (Exception e) {
            // Log but don't fail approval if organization linking fails
            System.err.println("Failed to automatically link user to organization: " + e.getMessage());
        }

        return ResponseEntity.ok(approval);
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<UserApproval> blockUser(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        String blockedBy = UserContextUtil.getUserIdFromRequestContext();
        String reason = request.get("reason");
        UserApproval approval = userApprovalService.blockUser(userId, blockedBy, reason);
        return ResponseEntity.ok(approval);
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<UserApproval> unblockUser(@PathVariable String userId) {
        String unblockedBy = UserContextUtil.getUserIdFromRequestContext();
        UserApproval approval = userApprovalService.unblockUser(userId, unblockedBy);
        return ResponseEntity.ok(approval);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserApproval> getUserApproval(@PathVariable String userId) {
        return userApprovalService.getUserApproval(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{userEmail}")
    public ResponseEntity<UserApproval> getUserApprovalByEmail(@PathVariable String userEmail) {
        return userApprovalService.getUserApprovalByEmail(userEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserApproval>> getAllUsers() {
        List<UserApproval> users = userApprovalService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<UserApproval>> getPendingUsers() {
        List<UserApproval> users = userApprovalService.getPendingUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/approved")
    public ResponseEntity<List<UserApproval>> getApprovedUsers() {
        List<UserApproval> users = userApprovalService.getApprovedUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<UserApproval>> getBlockedUsers() {
        List<UserApproval> users = userApprovalService.getBlockedUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}/is-approved")
    public ResponseEntity<Boolean> isUserApproved(@PathVariable String userId) {
        log.info("Received is-approved request for userId: {}", userId);
        boolean approved = userApprovalService.isUserApproved(userId);
        log.info("Response for is-approved request for userId {}: {}", userId, approved);
        return ResponseEntity.ok(approved);
    }

    @GetMapping("/{userId}/is-blocked")
    public ResponseEntity<Boolean> isUserBlocked(@PathVariable String userId) {
        boolean blocked = userApprovalService.isUserBlocked(userId);
        return ResponseEntity.ok(blocked);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserApproval(@PathVariable String userId) {
        userApprovalService.deleteUserApproval(userId);
        return ResponseEntity.noContent().build();
    }
}
