package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.UserApproval;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserApprovalRepository extends MongoRepository<UserApproval, String> {
    Optional<UserApproval> findByUserId(String userId);
    List<UserApproval> findByApproved(boolean approved);
    List<UserApproval> findByBlocked(boolean blocked);
    Optional<UserApproval> findByUserEmail(String userEmail);
    boolean existsByUserId(String userId);
}
