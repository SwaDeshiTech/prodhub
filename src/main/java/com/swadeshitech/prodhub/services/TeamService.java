package com.swadeshitech.prodhub.services;

import java.util.List;
import java.util.Set;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.entity.Team;
import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.TeamRequest;
import com.swadeshitech.prodhub.dto.TeamResponse;
import com.swadeshitech.prodhub.dto.TeamEmployeeUpdateRequest;

@Component
public interface TeamService {

    public TeamResponse addTeam(TeamRequest teamRequest);

    public TeamResponse getTeamDetail(String teamUUID);

    public List<DropdownDTO> getAllTeamsForDropdown();

    public TeamResponse updateTeamEmployees(String teamUUID, TeamEmployeeUpdateRequest updateRequest);

    List<DropdownDTO> getTeamByDepartment(Set<Team> teamList);
}