package com.swadeshitech.prodhub.services.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swadeshitech.prodhub.dto.TeamRequest;
import com.swadeshitech.prodhub.dto.TeamResponse;
import com.swadeshitech.prodhub.entity.Department;
import com.swadeshitech.prodhub.entity.Team;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.DepartmentRepository;
import com.swadeshitech.prodhub.repository.TeamRepository;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.services.TeamService;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public TeamResponse addTeam(TeamRequest teamRequest) {

        Team team = modelMapper.map(teamRequest, Team.class);
        if (Objects.isNull(team)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        
        team.setActive(Boolean.TRUE);

        Set<User> users = new HashSet();

        for(String employee : teamRequest.getEmployeeList()) {
            Optional<User> user = userRepository.findByEmailId(employee);
            if(user.isPresent()) {
                users.add(user.get());
            }
        }

        team.setEmployees(users);

        Optional<Department> department = departmentRepository.findByName(teamRequest.getDepartmentName());
        if(department.isPresent()) {
            team.setDepartments(Set.of(department.get()));
        } else {
            log.error("failed to get department", teamRequest.getDepartmentName());
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        saveTeamDetailToRepository(team);
        TeamResponse teamResponse = modelMapper.map(team, TeamResponse.class);
        return teamResponse;
    }

    @Override
    public TeamResponse getTeamDetail(String teamUUID) {

        if (StringUtils.isEmpty(teamUUID)) {
            log.error("team uuid is empty/null");
            throw new CustomException(ErrorCode.TEAM_UUID_NOT_FOUND);
        }

        Optional<Team> team = teamRepository.findByName(teamUUID);
        if (team.isEmpty()) {
            log.error("team not found");
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }

        TeamResponse teamResponse = modelMapper.map(team.get(), TeamResponse.class);
        return teamResponse;
    }

    private Team saveTeamDetailToRepository(Team team) {
        try {
            return teamRepository.save(team);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to save data ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }
    
}
