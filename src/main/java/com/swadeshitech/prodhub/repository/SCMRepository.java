package com.swadeshitech.prodhub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.SCM;

@Repository
public interface SCMRepository extends MongoRepository<SCM, String> {

}
