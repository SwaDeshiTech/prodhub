package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.DeploymentSet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface DeploymentSetRepository extends MongoRepository<DeploymentSet, String> {
}
