package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.swadeshitech.prodhub.utils.UserContextUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.swadeshitech.prodhub.dto.RoleResponse;
import com.swadeshitech.prodhub.dto.TabRequest;
import com.swadeshitech.prodhub.dto.TabResponse;
import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.entity.Tab;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.TabService;
import com.swadeshitech.prodhub.services.UserService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TabServiceImpl implements TabService {

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    UserService userService;

    @Override
    public TabResponse addTab(TabRequest request) {

        log.info("tab request {}", request);

        Set<Role> roles = fetchRoles(request.getRoles());

        Tab tab = new Tab();
        tab.setId(new ObjectId().toString());
        tab.setActive(true);
        tab.setLink(request.getLink());
        tab.setName(request.getName());
        tab.setSortOrder(request.getSortOrder());
        tab.setRoles(roles);

        if (Objects.nonNull(request.getChildren())) {
            Set<Tab> children = new HashSet<>();
            for (TabRequest tabRequest : request.getChildren()) {
                Tab child = new Tab();
                child.setId(new ObjectId().toString());
                child.setActive(true);
                child.setLink(tabRequest.getLink());
                child.setName(tabRequest.getName());
                child.setSortOrder(tabRequest.getSortOrder());
                child.setRoles(roles);
                children.add(child);
            }
            log.info("printing children {}", children.size());
            tab.setChildren(children);
        }

        if (StringUtils.hasText(request.getParentId())) {
            Map<String, Object> parentFilters = new HashMap<>();
            parentFilters.put("_id", new ObjectId(request.getParentId()));
            List<Tab> parentTabs = readTransactionService.findTabDetailsByFilters(parentFilters);
            if (!CollectionUtils.isEmpty(parentTabs)) {
                Tab parent = parentTabs.get(0);
                if (parent.getChildren() == null) {
                    parent.setChildren(new HashSet<>());
                }
                parent.getChildren().add(tab);
                writeTransactionService.saveTabToRepository(parent);
                return mapEntityToDTO(tab);
            }
        }

        writeTransactionService.saveTabToRepository(tab);

        return mapEntityToDTO(tab);
    }

    private Set<Role> fetchRoles(Set<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new HashSet<>();
        }
        List<ObjectId> ids = new ArrayList<>();
        for (String role : roleIds) {
            ids.add(new ObjectId(role));
        }
        Map<String, Object> filters = new HashMap<>();
        filters.put("_id", ids);
        List<Role> roles = readTransactionService.findRoleDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(roles)) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }
        return new HashSet<>(roles);
    }

    @Override
    public TabResponse updateTab(String id, TabRequest request) {
        log.info("updating tab {} with request {}", id, request);
        
        // 1. Try to find as a root tab
        Map<String, Object> filters = new HashMap<>();
        filters.put("_id", id);
        List<Tab> tabs = readTransactionService.findTabDetailsByFilters(filters);
        
        if (!CollectionUtils.isEmpty(tabs)) {
            Tab tab = tabs.get(0);
            updateTabFields(tab, request);
            writeTransactionService.saveTabToRepository(tab);
            return mapEntityToDTO(tab);
        }
        
        // 2. Try to find as a sub-tab
        Map<String, Object> parentFilters = new HashMap<>();
        parentFilters.put("children._id", id);
        List<Tab> parents = readTransactionService.findTabDetailsByFilters(parentFilters);
        
        if (!CollectionUtils.isEmpty(parents)) {
            for (Tab parent : parents) {
                if (parent.getChildren() != null) {
                    for (Tab child : parent.getChildren()) {
                        if (id.equals(child.getId())) {
                            updateTabFields(child, request);
                            writeTransactionService.saveTabToRepository(parent);
                            return mapEntityToDTO(child);
                        }
                    }
                }
            }
        }
        
        throw new CustomException(ErrorCode.TAB_NOT_FOUND);
    }

    private void updateTabFields(Tab tab, TabRequest request) {
        tab.setName(request.getName());
        tab.setLink(request.getLink());
        tab.setSortOrder(request.getSortOrder());
        tab.setRoles(fetchRoles(request.getRoles()));
    }

    @Override
    public void deleteTab(String id) {
        log.info("deleting tab {}", id);
        
        // 1. Try to delete as a root tab
        writeTransactionService.removeTabFromRepository(id);
        
        // 2. Also search if it's a sub-tab of any root tab
        // We look for any tab that has this ID in its children set
        Map<String, Object> filters = new HashMap<>();
        filters.put("children._id", id);
        List<Tab> parents = readTransactionService.findTabDetailsByFilters(filters);
        
        if (!CollectionUtils.isEmpty(parents)) {
            for (Tab parent : parents) {
                if (parent.getChildren() != null) {
                    boolean removed = parent.getChildren().removeIf(child -> id.equals(child.getId()));
                    if (removed) {
                        log.info("Removed sub-tab {} from parent {}", id, parent.getId());
                        writeTransactionService.saveTabToRepository(parent);
                    }
                }
            }
        }
    }

    @Override
    public List<TabResponse> getActiveTabsByUser() {

        String uuid = UserContextUtil.getUserIdFromRequestContext();
        Set<Role> roles = userService.getUserRoles(uuid);
        Set<String> userRoleIds = new HashSet<>();
        List<org.bson.types.ObjectId> ids = new ArrayList<>();

        log.info("user uuid : {}", uuid);

        if (Objects.nonNull(roles)) {
            for (Role role : roles) {
                ids.add(new org.bson.types.ObjectId(role.getId()));
                userRoleIds.add(role.getId());
            }
        }

        List<com.mongodb.DBRef> dbRefs = new ArrayList<>();
        for (ObjectId id : ids) {
            dbRefs.add(new com.mongodb.DBRef("roles", id));
        }

        Map<String, Object> filters = new HashMap<>();
        filters.put("roles", dbRefs);
        filters.put("children.roles", dbRefs);

        log.info("tab filters : {}", filters);

        List<Tab> tabs = readTransactionService.findTabDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(tabs)) {
            throw new CustomException(ErrorCode.TAB_NOT_FOUND);
        }

        List<TabResponse> activeTabs = new ArrayList<>();

        for (Tab tab : tabs) {
            TabResponse tabDto = mapEntityToDTOWithFilter(tab, userRoleIds);
            if (tabDto != null) {
                activeTabs.add(tabDto);
            }
        }

        activeTabs.sort((t1, t2) -> {
            int order1 = t1.getSortOrder() != null ? t1.getSortOrder() : Integer.MAX_VALUE;
            int order2 = t2.getSortOrder() != null ? t2.getSortOrder() : Integer.MAX_VALUE;
            return Integer.compare(order1, order2);
        });

        return activeTabs;
    }

    private TabResponse mapEntityToDTO(Tab tab) {
        List<TabResponse> childrenResponses = new ArrayList<>();
        if (tab.getChildren() != null && !tab.getChildren().isEmpty()) {
            for (Tab child : tab.getChildren()) {
                childrenResponses.add(mapEntityToDTO(child));
            }
            childrenResponses.sort((t1, t2) -> {
                int order1 = t1.getSortOrder() != null ? t1.getSortOrder() : Integer.MAX_VALUE;
                int order2 = t2.getSortOrder() != null ? t2.getSortOrder() : Integer.MAX_VALUE;
                return Integer.compare(order1, order2);
            });
        }

        List<RoleResponse> roleResponses = new ArrayList<>();
        if (tab.getRoles() != null) {
            for (Role role : tab.getRoles()) {
                roleResponses.add(RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .isDefault(role.isDefault())
                        .build());
            }
        }

        return TabResponse.builder()
                .id(tab.getId())
                .link(tab.getLink())
                .name(tab.getName())
                .sortOrder(tab.getSortOrder())
                .isActive(tab.isActive())
                .createdBy(tab.getCreatedBy())
                .createdTime(tab.getCreatedTime())
                .lastModifiedBy(tab.getLastModifiedBy())
                .lastModifiedTime(tab.getLastModifiedTime())
                .children(childrenResponses)
                .roles(roleResponses)
                .build();
    }

    private TabResponse mapEntityToDTOWithFilter(Tab tab, Set<String> userRoleIds) {
        boolean userHasAccessToThisTab = hasAccess(tab, userRoleIds);

        List<TabResponse> childrenResponses = new ArrayList<>();
        if (tab.getChildren() != null && !tab.getChildren().isEmpty()) {
            for (Tab child : tab.getChildren()) {
                TabResponse childDto = mapEntityToDTOWithFilter(child, userRoleIds);
                if (childDto != null) {
                    childrenResponses.add(childDto);
                }
            }
            childrenResponses.sort((t1, t2) -> {
                int order1 = t1.getSortOrder() != null ? t1.getSortOrder() : Integer.MAX_VALUE;
                int order2 = t2.getSortOrder() != null ? t2.getSortOrder() : Integer.MAX_VALUE;
                return Integer.compare(order1, order2);
            });
        }

        // If the user doesn't have access to this tab and it has no accessible children, skip it
        if (!userHasAccessToThisTab && childrenResponses.isEmpty()) {
            return null;
        }

        List<RoleResponse> roleResponses = new ArrayList<>();
        if (tab.getRoles() != null) {
            for (Role role : tab.getRoles()) {
                roleResponses.add(RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .isDefault(role.isDefault())
                        .build());
            }
        }

        return TabResponse.builder()
                .id(tab.getId())
                .link(tab.getLink())
                .name(tab.getName())
                .sortOrder(tab.getSortOrder())
                .isActive(tab.isActive())
                .createdBy(tab.getCreatedBy())
                .createdTime(tab.getCreatedTime())
                .lastModifiedBy(tab.getLastModifiedBy())
                .lastModifiedTime(tab.getLastModifiedTime())
                .children(childrenResponses)
                .roles(roleResponses)
                .build();
    }

    private boolean hasAccess(Tab tab, Set<String> userRoleIds) {
        if (CollectionUtils.isEmpty(tab.getRoles())) {
            return true;
        }
        return tab.getRoles().stream()
                .anyMatch(role -> userRoleIds.contains(role.getId()));
    }

}
