package com.swadeshitech.prodhub.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.Metadata;

@Repository
public interface MetaDataRepository extends MongoRepository<Metadata, String> {

    public Optional<Metadata> findById(String id);

    public Optional<Metadata> findByName(String name);
}
