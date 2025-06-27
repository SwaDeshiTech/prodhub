package com.swadeshitech.prodhub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.CloudProvider;

@Repository
public interface CloudProviderRepository extends MongoRepository<CloudProvider, String> {

}
