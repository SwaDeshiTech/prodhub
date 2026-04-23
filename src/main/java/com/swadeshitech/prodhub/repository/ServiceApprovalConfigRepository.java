package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.ServiceApprovalConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceApprovalConfigRepository extends MongoRepository<ServiceApprovalConfig, String> {
    Optional<ServiceApprovalConfig> findByServiceId(String serviceId);
    boolean existsByServiceId(String serviceId);
}
