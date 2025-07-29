package com.swadeshitech.prodhub.transaction.read;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.entity.CloudProvider;
import com.swadeshitech.prodhub.entity.Constants;
import com.swadeshitech.prodhub.entity.ResourceDetails;
import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.entity.Tab;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ConstantsRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ReadTransactionService {

    @Autowired
    private ConstantsRepository constantsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Constants getConstantByName(String name) {
        Optional<Constants> optionalConstant = constantsRepository.findByName(name);
        if (optionalConstant.isEmpty()) {
            throw new CustomException(ErrorCode.CONSTANTS_NOT_FOUND);
        }
        return optionalConstant.get();
    }

    public List<CloudProvider> findCloudProvidersByFilters(Map<String, Object> filters) {
        Query query = new Query();
        filters.forEach((key, value) -> {
            if (value != null) {
                query.addCriteria(Criteria.where(key).is(value));
            }
        });
        return mongoTemplate.find(query, CloudProvider.class);
    }

    public List<ResourceDetails> findResourceDetailsByFilters(Map<String, Object> filters) {
        Query query = new Query();
        filters.forEach((key, value) -> {
            if (value != null) {
                query.addCriteria(Criteria.where(key).is(value));
            }
        });
        return mongoTemplate.find(query, ResourceDetails.class);
    }

    public List<Role> findRoleDetailsByFilters(Map<String, Object> filters) {
        Query query = new Query();

        filters.forEach((key, value) -> {
            if (value != null) {
                if (value instanceof Iterable) {
                    Iterable<?> iterable = (Iterable<?>) value;
                    // Convert Iterable to List and check emptiness
                    List<Object> list = new ArrayList<>();
                    iterable.forEach(list::add);
                    if (!list.isEmpty()) {
                        query.addCriteria(Criteria.where(key).in(list));
                    }
                } else if (value.getClass().isArray()) {
                    // Convert array to List and check emptiness
                    Object[] arr = (Object[]) value;
                    if (arr.length > 0) {
                        query.addCriteria(Criteria.where(key).in(Arrays.asList(arr)));
                    }
                } else {
                    // Single value - use is()
                    query.addCriteria(Criteria.where(key).is(value));
                }
            }
        });
        return mongoTemplate.find(query, Role.class);
    }

    public List<Tab> findTabDetailsByFilters(Map<String, Object> filters) {
        Query query = new Query();

        filters.forEach((key, value) -> {
            if (value != null) {
                if (value instanceof Iterable) {
                    Iterable<?> iterable = (Iterable<?>) value;
                    // Convert Iterable to List and check emptiness
                    List<Object> list = new ArrayList<>();
                    iterable.forEach(list::add);
                    if (!list.isEmpty()) {
                        query.addCriteria(Criteria.where(key).in(list));
                    }
                } else if (value.getClass().isArray()) {
                    // Convert array to List and check emptiness
                    Object[] arr = (Object[]) value;
                    if (arr.length > 0) {
                        query.addCriteria(Criteria.where(key).in(Arrays.asList(arr)));
                    }
                } else {
                    // Single value - use is()
                    query.addCriteria(Criteria.where(key).is(value));
                }
            }
        });
        return mongoTemplate.find(query, Tab.class);
    }

    public List<User> findUserDetailsByFilters(Map<String, Object> filters) {
        Query query = new Query();
        filters.forEach((key, value) -> {
            if (value != null) {
                query.addCriteria(Criteria.where(key).is(value));
            }
        });
        return mongoTemplate.find(query, User.class);
    }
}
