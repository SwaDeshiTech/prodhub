package com.swadeshitech.prodhub.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.swadeshitech.prodhub.entity.Application;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {

    public Optional<Application> findByName(String name);
}
