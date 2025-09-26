package com.swadeshitech.prodhub.services.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import org.springframework.util.CollectionUtils;

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

    @Autowired
    ReadTransactionService readTransactionService;

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

        Set<Application> applications = new HashSet<>();
        for(String applicationId : teamRequest.getApplications()) {
            List<Application> application = readTransactionService.findApplicationByFilters(Map.of("_id", new ObjectId(applicationId)));
            if(CollectionUtils.isEmpty(application)) {
                log.error("Application ID could not be found {}", applicationId);
                throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
            }
            applications.add(application.getFirst());
        }
        team.setApplications(applications);

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

        return mapEntityToDTO(team.get());
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

        Set<Application> applications = new HashSet<>();
        if(!CollectionUtils.isEmpty(updateRequest.getApplications())) {
            for(String applicationId : updateRequest.getApplications()) {
                List<Application> application = readTransactionService.findApplicationByFilters(Map.of("_id", new ObjectId(applicationId)));
                if(CollectionUtils.isEmpty(application)) {
                    log.error("Application ID could not be found {}", applicationId);
                    throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
                }
                applications.add(application.getFirst());
            }
        }

        // Update team with new employees and managers
        team.setEmployees(newEmployees);
        team.setManagers(newManagers);
        team.setApplications(applications);
        saveTeamDetailToRepository(team);

        // Update all affected users
        for (User user : usersToUpdate) {
            userRepository.save(user);
        }

        return mapEntityToDTO(team);
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

    protected TeamResponse mapEntityToDTO(Team team) {

        List<UserResponse> teamMembers = new ArrayList<>();
        if(!CollectionUtils.isEmpty(team.getEmployees())) {
            for(User user : team.getEmployees()) {
                UserResponse userResponse = new UserResponse();
                userResponse.setUuid(user.getUuid());
                userResponse.setName(user.getName());
                userResponse.setEmailId(user.getEmailId());
                userResponse.setProfilePicture(user.getProfilePicture());
                teamMembers.add(userResponse);
            }
        }

        List<UserResponse> managers = new ArrayList<>();
        if(!CollectionUtils.isEmpty(team.getManagers())) {
            for(User user : team.getManagers()) {
                UserResponse userResponse = new UserResponse();
                userResponse.setUuid(user.getUuid());
                userResponse.setName(user.getName());
                userResponse.setEmailId(user.getEmailId());
                userResponse.setProfilePicture(user.getProfilePicture());
                managers.add(userResponse);
            }
        }

        List<ApplicationResponse> applicationResponses = new ArrayList<>();
        if(!CollectionUtils.isEmpty(team.getApplications())) {
            for(Application application : team.getApplications()) {
                ApplicationResponse applicationResponse = new ApplicationResponse();
                applicationResponse.setId(application.getId());
                applicationResponse.setActive(application.isActive());
                applicationResponse.setName(application.getName());
                applicationResponse.setDescription(application.getDescription());
                applicationResponses.add(applicationResponse);
            }
        }

        List<DepartmentResponse> departmentResponses = new ArrayList<>();
        if(!CollectionUtils.isEmpty(team.getDepartments())) {
            for(Department department : team.getDepartments()) {
                DepartmentResponse departmentResponse = new DepartmentResponse();
                departmentResponse.setActive(department.isActive());
                departmentResponse.setName(department.getName());
                departmentResponse.setDescription(department.getDescription());
                departmentResponses.add(departmentResponse);
            }
        }

        return TeamResponse.builder()
                .name(team.getName())
                .id(team.getId())
                .isActive(team.isActive())
                .description(team.getDescription())
                .employees(teamMembers)
                .managers(managers)
                .applications(applicationResponses)
                .departments(departmentResponses)
                .createdBy(team.getCreatedBy())
                .createdTime(team.getCreatedTime())
                .lastModifiedBy(team.getLastModifiedBy())
                .lastModifiedTime(team.getLastModifiedTime())
                .build();
    }
}
