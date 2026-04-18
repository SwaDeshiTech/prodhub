package com.swadeshitech.prodhub.repository;

import com.swadeshitech.prodhub.entity.ResourceAllocationRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceAllocationRuleRepository extends MongoRepository<ResourceAllocationRule, String> {
    List<ResourceAllocationRule> findByIsActiveTrueOrderByPriorityDesc();
}
