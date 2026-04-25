package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.OAuthProvider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthProviderRepository extends MongoRepository<OAuthProvider, String> {

    List<OAuthProvider> findAllByOrderBySortOrderAsc();

    Optional<OAuthProvider> findByProviderTypeAndIsActive(String providerType, boolean isActive);

    List<OAuthProvider> findByIsActiveTrueOrderBySortOrderAsc();

    Optional<OAuthProvider> findByName(String name);

    boolean existsByProviderType(String providerType);
}
