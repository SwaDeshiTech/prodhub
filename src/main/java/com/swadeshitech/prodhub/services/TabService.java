package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.TabRequest;
import com.swadeshitech.prodhub.dto.TabResponse;

@Component
public interface TabService {

    public TabResponse addTab(TabRequest request);

    public List<TabResponse> getActiveTabsByUser(String uuid);
}
