package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.entity.UserApproval;
import com.swadeshitech.prodhub.service.UserApprovalService;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-approvals")
@CrossOrigin(origins = "*")
public class UserApprovalController {

    @Autowired
    private UserApprovalService userApprovalService;

    @PostMapping
    public ResponseEntity<UserApproval> createUserApproval(@RequestBody UserApproval userApproval) {
        UserApproval created = userApprovalService.createUserApproval(userApproval);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<UserApproval> approveUser(@PathVariable String userId) {
        String approvedBy = UserContextUtil.getUserId();
        UserApproval approval = userApprovalService.approveUser(userId, approvedBy);
        return ResponseEntity.ok(approval);
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<UserApproval> blockUser(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        String blockedBy = UserContextUtil.getUserId();
        String reason = request.get("reason");
        UserApproval approval = userApprovalService.blockUser(userId, blockedBy, reason);
        return ResponseEntity.ok(approval);
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<UserApproval> unblockUser(@PathVariable String userId) {
        String unblockedBy = UserContextUtil.getUserId();
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
        boolean approved = userApprovalService.isUserApproved(userId);
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
