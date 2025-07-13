package com.swadeshitech.prodhub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.swadeshitech.prodhub.entity.Role;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
}
