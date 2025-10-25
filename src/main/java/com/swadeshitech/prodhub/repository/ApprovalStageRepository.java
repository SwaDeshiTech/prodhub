package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.ApprovalStage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalStageRepository extends MongoRepository<ApprovalStage, String> {
}
