package com.swadeshitech.prodhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.EphemeralEnvironment;

@Repository
public interface EphemeralEnvironmentRepository extends MongoRepository<EphemeralEnvironment, String> {
    
    public Optional<List<EphemeralEnvironment>> findByName(String uuid);

}
