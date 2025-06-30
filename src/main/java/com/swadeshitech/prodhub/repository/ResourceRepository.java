package com.swadeshitech.prodhub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.ResourceDetails;

@Repository
public interface ResourceRepository extends MongoRepository<ResourceDetails, String> {
}
