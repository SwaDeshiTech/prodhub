package com.swadeshitech.prodhub.service.impl;

import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.entity.UserApproval;
import com.swadeshitech.prodhub.repository.UserApprovalRepository;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.service.UserApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserApprovalServiceImpl implements UserApprovalService {

    @Autowired
    private UserApprovalRepository userApprovalRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserApproval createUserApproval(UserApproval userApproval) {
        return userApprovalRepository.save(userApproval);
    }

    @Override
    public UserApproval approveUser(String userId, String approvedBy) {
        Optional<UserApproval> existingApproval = userApprovalRepository.findByUserId(userId);
        UserApproval approval;

        if (existingApproval.isPresent()) {
            approval = existingApproval.get();
        } else {
            Optional<User> user = userRepository.findById(userId);
            if (!user.isPresent()) {
                throw new RuntimeException("User not found with id: " + userId);
            }
            approval = UserApproval.builder()
                    .userId(userId)
                    .userName(user.get().getName())
                    .userEmail(user.get().getEmailId())
                    .approved(false)
                    .blocked(false)
                    .build();
        }

        approval.setApproved(true);
        approval.setBlocked(false);
        approval.setApprovedBy(approvedBy);
        approval.setUpdatedAt(LocalDateTime.now());
        approval.setRejectionReason(null);

        return userApprovalRepository.save(approval);
    }

    @Override
    public UserApproval blockUser(String userId, String blockedBy, String reason) {
        Optional<UserApproval> existingApproval = userApprovalRepository.findByUserId(userId);
        UserApproval approval;

        if (existingApproval.isPresent()) {
            approval = existingApproval.get();
        } else {
            Optional<User> user = userRepository.findById(userId);
            if (!user.isPresent()) {
                throw new RuntimeException("User not found with id: " + userId);
            }
            approval = UserApproval.builder()
                    .userId(userId)
                    .userName(user.get().getName())
                    .userEmail(user.get().getEmailId())
                    .approved(false)
                    .blocked(false)
                    .build();
        }

        approval.setApproved(false);
        approval.setBlocked(true);
        approval.setBlockedBy(blockedBy);
        approval.setRejectionReason(reason);
        approval.setUpdatedAt(LocalDateTime.now());

        return userApprovalRepository.save(approval);
    }

    @Override
    public UserApproval unblockUser(String userId, String unblockedBy) {
        Optional<UserApproval> existingApproval = userApprovalRepository.findByUserId(userId);
        if (!existingApproval.isPresent()) {
            throw new RuntimeException("User approval not found for user id: " + userId);
        }

        UserApproval approval = existingApproval.get();
        approval.setBlocked(false);
        approval.setApproved(true);
        approval.setApprovedBy(unblockedBy);
        approval.setRejectionReason(null);
        approval.setUpdatedAt(LocalDateTime.now());

        return userApprovalRepository.save(approval);
    }

    @Override
    public Optional<UserApproval> getUserApproval(String userId) {
        return userApprovalRepository.findByUserId(userId);
    }

    @Override
    public Optional<UserApproval> getUserApprovalByEmail(String userEmail) {
        return userApprovalRepository.findByUserEmail(userEmail);
    }

    @Override
    public List<UserApproval> getAllUsers() {
        return userApprovalRepository.findAll();
    }

    @Override
    public List<UserApproval> getPendingUsers() {
        return userApprovalRepository.findByApproved(false);
    }

    @Override
    public List<UserApproval> getApprovedUsers() {
        return userApprovalRepository.findByApproved(true);
    }

    @Override
    public List<UserApproval> getBlockedUsers() {
        return userApprovalRepository.findByBlocked(true);
    }

    @Override
    public boolean isUserApproved(String userId) {
        Optional<UserApproval> approval = userApprovalRepository.findByUserId(userId);
        if (!approval.isPresent()) {
            return false;
        }
        return approval.get().isApproved() && !approval.get().isBlocked();
    }

    @Override
    public boolean isUserBlocked(String userId) {
        Optional<UserApproval> approval = userApprovalRepository.findByUserId(userId);
        if (!approval.isPresent()) {
            return false;
        }
        return approval.get().isBlocked();
    }

    @Override
    public void deleteUserApproval(String userId) {
        Optional<UserApproval> approval = userApprovalRepository.findByUserId(userId);
        if (approval.isPresent()) {
            userApprovalRepository.delete(approval.get());
        }
    }
}
