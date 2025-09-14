package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.Approvals;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalsRepository extends MongoRepository<Approvals, String> {
}
