package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.Deployment;
import com.swadeshitech.prodhub.entity.DeploymentRun;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeploymentRunRepository extends MongoRepository<DeploymentRun, String> {
}
