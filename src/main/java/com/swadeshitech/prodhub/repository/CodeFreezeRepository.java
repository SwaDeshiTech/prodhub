package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.CodeFreeze;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface CodeFreezeRepository extends MongoRepository<CodeFreeze, String> {
}
