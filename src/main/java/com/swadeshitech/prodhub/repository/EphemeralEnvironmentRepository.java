package com.swadeshitech.prodhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.swadeshitech.prodhub.entity.EphemeralEnvironment;

public interface EphemeralEnvironmentRepository extends JpaRepository<EphemeralEnvironment, String>, JpaSpecificationExecutor<EphemeralEnvironment> {
    
    public Optional<List<EphemeralEnvironment>> findByUuid(String uuid);

}
