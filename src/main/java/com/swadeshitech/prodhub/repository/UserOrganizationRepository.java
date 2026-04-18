package com.swadeshitech.prodhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.UserOrganization;

@Repository
public interface UserOrganizationRepository extends MongoRepository<UserOrganization, String> {

    List<UserOrganization> findByUserIdAndActiveTrue(String userId);

    List<UserOrganization> findByOrganizationIdAndActiveTrue(String organizationId);

    Optional<UserOrganization> findByUserIdAndOrganizationIdAndActiveTrue(String userId, String organizationId);

    boolean existsByUserIdAndOrganizationIdAndActiveTrue(String userId, String organizationId);
}
