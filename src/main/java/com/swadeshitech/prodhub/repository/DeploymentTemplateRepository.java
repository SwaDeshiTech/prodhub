package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.DeploymentTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeploymentTemplateRepository extends MongoRepository<DeploymentTemplate, String> {
}
