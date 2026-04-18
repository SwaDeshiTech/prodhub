package com.swadeshitech.prodhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.Token;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {

    Optional<Token> findByTokenId(String tokenId);

    List<Token> findByUserIdAndActiveTrue(String userId);

    List<Token> findByOrganizationIdAndActiveTrue(String organizationId);
}
