package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

        Map<String, Object> filters = new HashMap<>();
        filters.put("id", request.getRoles());

        List<Role> roles = readTransactionService.findRoleDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(roles)) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        Tab tab = new Tab();
        tab.setActive(true);
        tab.setLink(request.getLink());
        tab.setName(request.getName());
        tab.setRoles(new HashSet<>(roles));

        Set<Tab> children = new HashSet<>();

        for (TabRequest tabRequest : request.getChildren()) {
            Tab child = new Tab();
            child.setActive(true);
            child.setLink(tabRequest.getLink());
            child.setName(tabRequest.getName());
            child.setRoles(new HashSet<>(roles));
            children.add(child);
        }

        tab.setChildren(children);

        writeTransactionService.saveTabToRepository(tab);

        return mapEntityToDTO(tab);
    }

    @Override
    public List<TabResponse> getActiveTabsByUser(String uuid) {

        Set<Role> roles = userService.getUserRoles(uuid);

        Map<String, Object> filters = new HashMap<>();
        filters.put("roles", roles);

        List<Tab> tabs = readTransactionService.findTabDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(tabs)) {
            throw new CustomException(ErrorCode.TAB_NOT_FOUND);
        }

        List<TabResponse> activeTabs = new ArrayList<>();

        for (Tab tab : tabs) {
            TabResponse tabResponse = mapEntityToDTO(tab);
            tabResponse.setChildren(new ArrayList<>());
            for (Tab child : tab.getChildren()) {
                tabResponse.getChildren().add(mapEntityToDTO(child));
            }
        }

        return activeTabs;
    }

    private TabResponse mapEntityToDTO(Tab tab) {
        return TabResponse.builder()
                .id(tab.getId())
                .link(tab.getLink())
                .name(tab.getName())
                .createdBy(tab.getCreatedBy())
                .createdTime(tab.getCreatedTime())
                .lastModifiedBy(tab.getLastModifiedBy())
                .lastModifiedTime(tab.getLastModifiedTime())
                .build();
    }

}
