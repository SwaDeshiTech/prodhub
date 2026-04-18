package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.K8sClusterGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface K8sClusterGroupRepository extends MongoRepository<K8sClusterGroup, String> {
}
