package com.swadeshitech.prodhub.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.swadeshitech.prodhub.entity.Department;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {

    Optional<Department> findByName(String name);
}
