package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.Deployment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeploymentRepository extends MongoRepository<Deployment, String> {
    List<Deployment> findTop5ByOrderByCreatedTimeDesc();
}
