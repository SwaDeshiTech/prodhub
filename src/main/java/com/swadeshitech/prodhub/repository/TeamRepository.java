package com.swadeshitech.prodhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import com.swadeshitech.prodhub.entity.Team;

@Repository
@EnableJpaRepositories
public interface TeamRepository extends JpaRepository<Team, String>, JpaSpecificationExecutor<Team> {

    Optional<Team> findById(String id);
}
