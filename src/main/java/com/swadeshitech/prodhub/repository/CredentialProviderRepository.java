package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.CredentialProvider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialProviderRepository extends MongoRepository<CredentialProvider, String> {
}
