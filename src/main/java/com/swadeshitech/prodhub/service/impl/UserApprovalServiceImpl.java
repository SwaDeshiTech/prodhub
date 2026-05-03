package com.swadeshitech.prodhub.service.impl;

import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.entity.UserApproval;
import com.swadeshitech.prodhub.entity.UserOrganization;
import com.swadeshitech.prodhub.repository.UserApprovalRepository;
import com.swadeshitech.prodhub.repository.UserOrganizationRepository;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.service.UserApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("userApprovalService")
@Slf4j
public class UserApprovalServiceImpl implements UserApprovalService {

    @Autowired
    private UserApprovalRepository userApprovalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserOrganizationRepository userOrganizationRepository;

    @Override
    public UserApproval createUserApproval(UserApproval userApproval) {
        return userApprovalRepository.save(userApproval);
    }

    @Override
    public UserApproval createOrUpdatePendingApproval(User user) {
        Optional<UserApproval> existing = userApprovalRepository.findByUserId(user.getId());
        if (existing.isPresent()) {
            UserApproval approval = existing.get();
            // If already approved or blocked, don't reset it
            if (approval.isApproved() || approval.isBlocked()) {
                return approval;
            }
            return approval;
        }

        UserApproval pending = UserApproval.builder()
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmailId())
                .approved(false)
                .blocked(false)
                .build();
        pending.setCreatedTime(LocalDateTime.now());
        pending.setUpdatedAt(LocalDateTime.now());
        
        return userApprovalRepository.save(pending);
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
    public boolean isUserApproved(String identifier) {
        log.info("Checking approval for identifier: {}", identifier);
        // 1. Try by userId (which refers to User.id)
        Optional<UserApproval> approval = userApprovalRepository.findByUserId(identifier);
        
        // 2. Try by userEmail
        if (approval.isEmpty()) {
            approval = userApprovalRepository.findByUserEmail(identifier);
        }
        
        // 3. Try by finding user by uuid first, then checking approval by User.id
        if (approval.isEmpty()) {
            Optional<User> user = userRepository.findByUuid(identifier);
            if (user.isPresent()) {
                log.info("Found user by UUID: {}", user.get().getId());
                approval = userApprovalRepository.findByUserId(user.get().getId());
            }
        }
        
        if (approval.isPresent()) {
            log.info("Found approval entry. Blocked: {}, Approved: {}", approval.get().isBlocked(), approval.get().isApproved());
            if (approval.get().isBlocked()) {
                return false;
            }
            if (approval.get().isApproved()) {
                return true;
            }
        }
        
        // If not approved yet, check if they are an owner of any organization
        boolean isOwner = isOwnerOfAnyOrganization(identifier);
        log.info("Is owner check for {}: {}", identifier, isOwner);
        return isOwner;
    }

    private boolean isOwnerOfAnyOrganization(String identifier) {
        log.info("Starting ownership check for identifier: {}", identifier);
        
        // Collect all possible identifiers for this user
        java.util.Set<String> allIdentifiers = new java.util.HashSet<>();
        allIdentifiers.add(identifier);
        
        Optional<User> userOpt = userRepository.findById(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUuid(identifier);
        }
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmailId(identifier);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getId() != null) allIdentifiers.add(user.getId());
            if (user.getUuid() != null) allIdentifiers.add(user.getUuid());
            if (user.getEmailId() != null) allIdentifiers.add(user.getEmailId());
            log.info("Found user record. Checking all identifiers: {}", allIdentifiers);
        } else {
            log.warn("No user record found for identifier: {}. Proceeding with direct lookup.", identifier);
        }
        
        // Check for OWNER role across all identifiers
        for (String id : allIdentifiers) {
            List<UserOrganization> orgs = userOrganizationRepository.findByUserIdAndIsActiveTrue(id);
            if (!orgs.isEmpty()) {
                log.info("Found {} organizations for identifier: {}", orgs.size(), id);
                for (UserOrganization org : orgs) {
                    log.info("User {} has role {} in org {}", id, org.getRole(), org.getOrganizationId());
                    if ("OWNER".equalsIgnoreCase(org.getRole())) {
                        log.info("OWNER role detected for user {} in org {}. Approval granted.", id, org.getOrganizationId());
                        return true;
                    }
                }
            }
        }
        
        log.info("No OWNER role found for any identifier associated with: {}", identifier);
        return false;
    }

    @Override
    public boolean isUserBlocked(String identifier) {
        // 1. Try by userId
        Optional<UserApproval> approval = userApprovalRepository.findByUserId(identifier);
        
        // 2. Try by userEmail
        if (approval.isEmpty()) {
            approval = userApprovalRepository.findByUserEmail(identifier);
        }
        
        // 3. Try by uuid
        if (approval.isEmpty()) {
            Optional<User> user = userRepository.findByUuid(identifier);
            if (user.isPresent()) {
                approval = userApprovalRepository.findByUserId(user.get().getId());
            }
        }
        
        if (approval.isEmpty()) {
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
