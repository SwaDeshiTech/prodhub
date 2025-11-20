package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.Deployment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeploymentRepository extends MongoRepository<Deployment, String> {
}
