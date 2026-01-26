package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.PipelineTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineTemplateRepository extends MongoRepository<PipelineTemplate, String> {
}
