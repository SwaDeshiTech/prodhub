package com.swadeshitech.prodhub.transaction.read;

import com.swadeshitech.prodhub.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ConstantsRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;

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
        return mongoTemplate.find(query, User.class);
    }

    public List<SCM> findSCMDetailsByFilters(Map<String, Object> filters) {
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
        return mongoTemplate.find(query, SCM.class);
    }

    public List<Organization> findOrganizationDetailsByFilters(Map<String, Object> filters) {
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
        return mongoTemplate.find(query, Organization.class);
    }

    public List<ReleaseCandidate> findReleaseCandidateDetailsByFilters(Map<String, Object> filters) {
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
        return mongoTemplate.find(query, ReleaseCandidate.class);
    }

    public List<Metadata> findMetaDataByFilters(Map<String, Object> filters) {
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
        return mongoTemplate.find(query, Metadata.class);
    }

    public List<Application> findApplicationByFilters(Map<String, Object> filters) {
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
        return mongoTemplate.find(query, Application.class);
    }

    public List<CredentialProvider> findCredentialProviderByFilters(Map<String, Object> filters) {
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
        return mongoTemplate.find(query, CredentialProvider.class);
    }

    public List<Approvals> findApprovalsByFilters(Map<String, Object> filters) {
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
        return mongoTemplate.find(query, Approvals.class);
    }

    public List<CodeFreeze> findCodeFreezeByFilters(Map<String, Object> filters) {
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
        return mongoTemplate.find(query, CodeFreeze.class);
    }

    public <T> List<T> findByDynamicOrFilters(Map<String, Object> filters, Class<T> clazz) {
        if (filters == null || filters.isEmpty()) {
            return Collections.emptyList();
        }
        List<Criteria> orCriterias = new ArrayList<>();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                orCriterias.add(Criteria.where(key).is(value));
            }
        }
        Criteria orCriteria = new Criteria().orOperator(orCriterias.toArray(new Criteria[0]));
        Query query = new Query(orCriteria);
        return mongoTemplate.find(query, clazz);
    }

    public <T> Page<T> findByDynamicOrFiltersPaginated(
            Map<String, Object> filters,
            Class<T> clazz,
            Integer page,
            Integer size,
            String sortBy,
            Sort.Direction direction) {

        int finalPage = (page != null) ? page : 0;
        int finalSize = (size != null) ? size : 10;
        String finalSort = StringUtils.hasText(sortBy) ? sortBy : "createdTime";
        Sort.Direction finalDir = (direction != null) ? direction : Sort.Direction.DESC;

        Query query = new Query();

        if (filters != null && !filters.isEmpty()) {
            List<Criteria> criteriaList = new ArrayList<>();
            filters.forEach((key, value) -> {
                if (value != null) {
                    criteriaList.add(Criteria.where(key).is(value));
                }
            });

            if (criteriaList.size() == 1) {
                query.addCriteria(criteriaList.get(0));
            } else if (criteriaList.size() > 1) {
                query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
            }
        }

        Pageable pageable = PageRequest.of(finalPage, finalSize, Sort.by(finalDir, finalSort));
        query.with(pageable);

        log.info("Executing Mongo Query: {}", query);

        List<T> list = mongoTemplate.find(query, clazz);
        return PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), clazz)
        );
    }

}
