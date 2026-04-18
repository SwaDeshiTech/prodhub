package com.swadeshitech.prodhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.UserOrganization;

@Repository
public interface UserOrganizationRepository extends MongoRepository<UserOrganization, String> {

    List<UserOrganization> findByUserIdAndIsActiveTrue(String userId);

    List<UserOrganization> findByOrganizationIdAndIsActiveTrue(String organizationId);

    Optional<UserOrganization> findByUserIdAndOrganizationIdAndIsActiveTrue(String userId, String organizationId);

    boolean existsByUserIdAndOrganizationIdAndIsActiveTrue(String userId, String organizationId);
}
