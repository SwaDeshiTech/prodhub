package com.swadeshitech.prodhub.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.Constants;

@Repository
public interface ConstantsRepository extends MongoRepository<Constants, String> {

    Optional<Constants> findByName(String name);
}
