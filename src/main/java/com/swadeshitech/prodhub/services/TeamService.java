package com.swadeshitech.prodhub.services;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.TeamRequest;
import com.swadeshitech.prodhub.dto.TeamResponse;

@Component
public interface TeamService {
    
    public TeamResponse addTeam(TeamRequest teamRequest);

    public TeamResponse getTeamDetail(String teamUUID);
    
}