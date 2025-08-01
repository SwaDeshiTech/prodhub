package com.swadeshitech.prodhub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.BuildProvider;

@Repository
public interface BuildProviderRepository extends MongoRepository<BuildProvider, String> {

}
