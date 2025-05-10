package com.swadeshitech.prodhub.services.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swadeshitech.prodhub.dto.TeamRequest;
import com.swadeshitech.prodhub.dto.TeamResponse;
import com.swadeshitech.prodhub.dto.TeamEmployeeUpdateRequest;
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

        Set<User> users = new HashSet<>();
        if (Objects.nonNull(teamRequest.getEmployeeList())) {
            for (String id : teamRequest.getEmployeeList()) {
                Optional<User> user = userRepository.findByUuid(id);
                user.ifPresent(users::add);
            }
        }
        team.setEmployees(users);

        Set<User> managers = new HashSet<>();
        if (Objects.nonNull(teamRequest.getManagerList())) {
            for (String uuid : teamRequest.getManagerList()) {
                Optional<User> user = userRepository.findByUuid(uuid);
                user.ifPresent(managers::add);
            }
        }
        team.setManagers(managers);

        Optional<Department> departmentOptional = departmentRepository.findById(teamRequest.getDepartmentId());
        if (departmentOptional.isEmpty()) {
            log.error("failed to get department {}", teamRequest.getDepartmentId());
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        Department department = departmentOptional.get();
        // Set department in team
        team.setDepartments(Set.of(department));

        // Add team to department's teams
        Set<Team> departmentTeams = department.getTeams();
        if (departmentTeams == null) {
            departmentTeams = new HashSet<>();
        }
        departmentTeams.add(team);
        department.setTeams(departmentTeams);

        // Save both team and department
        saveTeamDetailToRepository(team);
        departmentRepository.save(department);

        return modelMapper.map(team, TeamResponse.class);
    }

    @Override
    public TeamResponse getTeamDetail(String teamUUID) {

        if (StringUtils.isEmpty(teamUUID)) {
            log.error("team uuid is empty/null");
            throw new CustomException(ErrorCode.TEAM_UUID_NOT_FOUND);
        }

        Optional<Team> team = teamRepository.findById(teamUUID);
        if (team.isEmpty()) {
            log.error("team not found {}", teamUUID);
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }

        return modelMapper.map(team.get(), TeamResponse.class);
    }

    @Override
    public List<DropdownDTO> getAllTeamsForDropdown() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .map(team -> new DropdownDTO(team.getId(), team.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeamResponse updateTeamEmployees(String teamUUID, TeamEmployeeUpdateRequest updateRequest) {
        if (StringUtils.isEmpty(teamUUID)) {
            log.error("team uuid is empty/null {}", teamUUID);
            throw new CustomException(ErrorCode.TEAM_UUID_NOT_FOUND);
        }

        Optional<Team> teamOptional = teamRepository.findById(teamUUID);
        if (teamOptional.isEmpty()) {
            log.error("team not found {}", teamUUID);
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }

        Team team = teamOptional.get();
        team.setDescription(updateRequest.getDescription());
        Set<User> newEmployees = new HashSet<>();
        Set<User> newManagers = new HashSet<>();
        Set<User> usersToUpdate = new HashSet<>();

        // First, remove team from all current employees and their departments
        Set<User> currentEmployees = team.getEmployees();
        if (currentEmployees != null) {
            for (User currentEmployee : currentEmployees) {
                // Remove team from user's teams
                Set<Team> userTeams = currentEmployee.getTeams();
                if (userTeams != null) {
                    userTeams.remove(team);
                    currentEmployee.setTeams(userTeams);
                }

                // Remove team's departments from user's departments
                Set<Department> userDepartments = currentEmployee.getDepartments();
                if (userDepartments != null) {
                    userDepartments.removeAll(team.getDepartments());
                    currentEmployee.setDepartments(userDepartments);
                }

                usersToUpdate.add(currentEmployee);
            }
        }

        // First, remove team from all current managers and their departments
        Set<User> currentManagers = team.getManagers();
        if (currentManagers != null) {
            for (User currentManager : currentManagers) {
                // Remove team from user's teams
                Set<Team> userTeams = currentManager.getTeams();
                if (userTeams != null) {
                    userTeams.remove(team);
                    currentManager.setTeams(userTeams);
                }

                // Remove team's departments from user's departments
                Set<Department> userDepartments = currentManager.getDepartments();
                if (userDepartments != null) {
                    userDepartments.removeAll(team.getDepartments());
                    currentManager.setDepartments(userDepartments);
                }

                usersToUpdate.add(currentManager);
            }
        }

        if (Objects.nonNull(updateRequest.getEmployeeList())) {
            // Then, add new employees and update their teams and departments
            for (String uuid : updateRequest.getEmployeeList()) {
                Optional<User> userOptional = userRepository.findByUuid(uuid);
                if (userOptional.isEmpty()) {
                    log.error("user not found with uuid: {}", uuid);
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                }

                User user = userOptional.get();
                newEmployees.add(user);

                // Add team to user's teams
                Set<Team> userTeams = user.getTeams();
                if (userTeams == null) {
                    userTeams = new HashSet<>();
                }
                userTeams.add(team);
                user.setTeams(userTeams);

                // Add team's departments to user's departments
                Set<Department> userDepartments = user.getDepartments();
                if (userDepartments == null) {
                    userDepartments = new HashSet<>();
                }
                userDepartments.addAll(team.getDepartments());
                user.setDepartments(userDepartments);

                usersToUpdate.add(user);
            }
        }

        if (Objects.nonNull(updateRequest.getManagerList())) {
            // Then, add new managers and update their teams and departments
            for (String uuid : updateRequest.getManagerList()) {
                Optional<User> userOptional = userRepository.findByUuid(uuid);
                if (userOptional.isEmpty()) {
                    log.error("user not found with uuid: {}", uuid);
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                }

                User user = userOptional.get();
                newManagers.add(user);

                // Add team to user's teams
                Set<Team> userTeams = user.getTeams();
                if (userTeams == null) {
                    userTeams = new HashSet<>();
                }
                userTeams.add(team);
                user.setTeams(userTeams);

                // Add team's departments to user's departments
                Set<Department> userDepartments = user.getDepartments();
                if (userDepartments == null) {
                    userDepartments = new HashSet<>();
                }
                userDepartments.addAll(team.getDepartments());
                user.setDepartments(userDepartments);

                usersToUpdate.add(user);
            }
        }

        // Update team with new employees and managers
        team.setEmployees(newEmployees);
        team.setManagers(newManagers);
        saveTeamDetailToRepository(team);

        // Update all affected users
        for (User user : usersToUpdate) {
            userRepository.save(user);
        }

        return modelMapper.map(team, TeamResponse.class);
    }

    @Override
    public List<DropdownDTO> getTeamByDepartment(Set<Team> teamList) {
        if (Objects.isNull(teamList)) {
            return Collections.emptyList();
        }
        List<DropdownDTO> dropdownDTOList = new ArrayList<>();
        for (Team team : teamList) {
            dropdownDTOList.add(new DropdownDTO(team.getId(), team.getName()));
        }
        return dropdownDTOList;
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
