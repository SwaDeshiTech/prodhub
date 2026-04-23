package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.FeatureFlag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeatureFlagRepository extends MongoRepository<FeatureFlag, String> {

    Optional<FeatureFlag> findByKey(String key);

    boolean existsByKey(String key);
}
