package com.swadeshitech.prodhub.service;

import com.swadeshitech.prodhub.entity.UserApproval;

import java.util.List;
import java.util.Optional;

public interface UserApprovalService {
    UserApproval createUserApproval(UserApproval userApproval);
    UserApproval approveUser(String userId, String approvedBy);
    UserApproval blockUser(String userId, String blockedBy, String reason);
    UserApproval unblockUser(String userId, String unblockedBy);
    Optional<UserApproval> getUserApproval(String userId);
    Optional<UserApproval> getUserApprovalByEmail(String userEmail);
    List<UserApproval> getAllUsers();
    List<UserApproval> getPendingUsers();
    List<UserApproval> getApprovedUsers();
    List<UserApproval> getBlockedUsers();
    boolean isUserApproved(String userId);
    boolean isUserBlocked(String userId);
    void deleteUserApproval(String userId);
}
