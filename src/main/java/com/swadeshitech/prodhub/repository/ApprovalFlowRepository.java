package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.ApprovalFlow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalFlowRepository extends MongoRepository<ApprovalFlow, String> {
    Optional<ApprovalFlow> findByKey(String key);
    Optional<ApprovalFlow> findByIsDefaultTrue();
    List<ApprovalFlow> findByIsActive(boolean isActive);
    boolean existsByKey(String key);
}
