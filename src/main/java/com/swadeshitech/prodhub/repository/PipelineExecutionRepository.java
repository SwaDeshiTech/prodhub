package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.PipelineExecution;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineExecutionRepository extends MongoRepository<PipelineExecution, String> {
}
