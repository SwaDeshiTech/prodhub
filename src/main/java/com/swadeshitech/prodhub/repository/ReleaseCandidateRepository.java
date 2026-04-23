package com.swadeshitech.prodhub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.ReleaseCandidate;

import java.util.List;

@Repository
public interface ReleaseCandidateRepository extends MongoRepository<ReleaseCandidate, String> {
    List<ReleaseCandidate> findTop5ByOrderByCreatedTimeDesc();
}
