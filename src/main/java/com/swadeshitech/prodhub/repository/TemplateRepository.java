package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.Template;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends MongoRepository<Template, String> {
}
