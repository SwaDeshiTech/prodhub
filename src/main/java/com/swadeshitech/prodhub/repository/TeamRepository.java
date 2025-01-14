package com.swadeshitech.prodhub.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.Team;

@Repository
public interface TeamRepository extends MongoRepository<Team, String> {

    Optional<Team> findByName(Long id);
}
