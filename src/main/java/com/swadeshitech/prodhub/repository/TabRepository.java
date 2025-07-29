package com.swadeshitech.prodhub.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.swadeshitech.prodhub.entity.Tab;

@Repository
public interface TabRepository extends MongoRepository<Tab, String> {
}
