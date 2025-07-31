package com.swadeshitech.prodhub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.Organization;

@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {
}
