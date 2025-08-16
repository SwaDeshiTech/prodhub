package com.swadeshitech.prodhub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.ReleaseCandidate;

@Repository
public interface ReleaseCandidateRepository extends MongoRepository<ReleaseCandidate, String> {
}
