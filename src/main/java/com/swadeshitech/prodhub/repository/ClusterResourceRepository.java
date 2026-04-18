package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.ClusterResource;
import com.swadeshitech.prodhub.entity.CredentialProvider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterResourceRepository extends MongoRepository<ClusterResource, String> {
    List<ClusterResource> findByCluster(CredentialProvider cluster);
}
