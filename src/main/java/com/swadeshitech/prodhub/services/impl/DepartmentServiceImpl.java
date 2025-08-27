package com.swadeshitech.prodhub.services.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.swadeshitech.prodhub.services.TeamService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.swadeshitech.prodhub.dto.DepartmentRequest;
import com.swadeshitech.prodhub.dto.DepartmentResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.entity.Department;
import com.swadeshitech.prodhub.entity.Team;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.DepartmentRepository;
import com.swadeshitech.prodhub.repository.TeamRepository;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.services.DepartmentService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private TeamService teamService;

    @Override
    public DepartmentResponse addDepartment(DepartmentRequest departmentRequest) {
        Department department = modelMapper.map(departmentRequest, Department.class);
        if (Objects.isNull(department)) {
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        department.setActive(Boolean.TRUE);

        // Set head of department
        if (Objects.nonNull(departmentRequest.getHeadOfDepartment())) {
            for (String headOfDepartmentUUID : departmentRequest.getHeadOfDepartment()) {
                Optional<User> user = userRepository.findByUuid(headOfDepartmentUUID);
                if (user.isPresent()) {
                    Set<User> headOfDepartment = new HashSet<>();
                    headOfDepartment.add(user.get());
                    department.setHeadOfDepartment(headOfDepartment);
                }
            }
        }

        // Set teams
        if (Objects.nonNull(departmentRequest.getTeams())) {
            Set<Team> teams = new HashSet<>();
            for (String teamUUID : departmentRequest.getTeams()) {
                Optional<Team> team = teamRepository.findById(teamUUID);
                team.ifPresent(teams::add);
            }
            department.setTeams(teams);
        }

        saveDepartmentDetailToRepository(department);

        return modelMapper.map(department, DepartmentResponse.class);
    }

    @Override
    public DepartmentResponse getDepartmentDetail(String id) {
        if (StringUtils.isEmpty(id)) {
            log.error("department name is empty/null");
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        Optional<Department> department = departmentRepository.findById(id);
        if (department.isEmpty()) {
            log.error("department not found");
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        List<DropdownDTO> teams = teamService.getTeamByDepartment(department.get().getTeams());
        List<DropdownDTO> headOfDepartment = new ArrayList<>();
        if (Objects.nonNull(department.get().getHeadOfDepartment())) {
            for (User user : department.get().getHeadOfDepartment()) {
                headOfDepartment.add(new DropdownDTO(user.getUuid(), user.getName() + " (" + user.getEmailId() + ")"));
            }
        }

        DepartmentResponse departmentResponse = modelMapper.map(department.get(), DepartmentResponse.class);
        departmentResponse.setTeams(teams);
        departmentResponse.setHeadOfDepartment(headOfDepartment);
        departmentResponse.setCreatedBy(department.get().getCreatedBy());
        departmentResponse.setCreatedTime(department.get().getCreatedTime());
        departmentResponse.setLastModifiedBy(department.get().getLastModifiedBy());
        departmentResponse.setLastModifiedTime(department.get().getLastModifiedTime());
        return departmentResponse;
    }

    @Override
    public List<DropdownDTO> getAllDepartmentsForDropdown() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                .map(department -> new DropdownDTO(department.getId(), department.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String updateDepartment(String departmentUUID, DepartmentRequest departmentRequest) {
        Department department = departmentRepository.findById(departmentUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));

        Set<User> usersToSave = new HashSet<>();

        // --- Update head of department ---
        if (Objects.nonNull(departmentRequest.getHeadOfDepartment())) {
            Set<User> newHeadOfDepartment = new HashSet<>();
            Set<String> newHeadUuids = new HashSet<>(departmentRequest.getHeadOfDepartment());

            // Fetch new head users
            for (String headUuid : newHeadUuids) {
                userRepository.findByUuid(headUuid).ifPresent(newHeadOfDepartment::add);
                // Add error handling if user not found?
            }

            Set<User> currentHeads = department.getHeadOfDepartment() == null ? new HashSet<>()
                    : new HashSet<>(department.getHeadOfDepartment());
            Set<Team> departmentTeams = department.getTeams() == null ? new HashSet<>() : department.getTeams();

            // Identify added and removed heads
            Set<User> addedHeads = new HashSet<>(newHeadOfDepartment);
            addedHeads.removeAll(currentHeads);

            Set<User> removedHeads = new HashSet<>(currentHeads);
            removedHeads.removeAll(newHeadOfDepartment);

            // Process added heads
            for (User addedHead : addedHeads) {
                // Add department to user's departments
                if (addedHead.getDepartments() == null) {
                    addedHead.setDepartments(new HashSet<>());
                }
                addedHead.getDepartments().add(department);

                // Add department's teams to user's teams
                if (!departmentTeams.isEmpty()) {
                    if (addedHead.getTeams() == null) {
                        addedHead.setTeams(new HashSet<>());
                    }
                    addedHead.getTeams().addAll(departmentTeams);
                }
                usersToSave.add(addedHead);
            }

            // Process removed heads
            for (User removedHead : removedHeads) {
                // Remove department from user's departments
                if (removedHead.getDepartments() != null) {
                    removedHead.getDepartments().remove(department);
                }
                // Optionally remove teams? Decided against for now as user might be in teams
                // for other reasons.
                usersToSave.add(removedHead);
            }

            // Update department's head list
            department.setHeadOfDepartment(newHeadOfDepartment);
        }

        // --- Update teams ---
        if (Objects.nonNull(departmentRequest.getTeams())) {
            // This part might need similar logic if team changes should affect users
            Set<Team> teams = new HashSet<>();
            for (String teamUUID : departmentRequest.getTeams()) {
                Optional<Team> team = teamRepository.findById(teamUUID);
                team.ifPresent(teams::add);
            }
            department.setTeams(teams);
            // Consider updating team members if necessary
        }

        // --- Update description ---
        if (Objects.nonNull(departmentRequest.getDescription())) {
            department.setDescription(departmentRequest.getDescription());
        }

        // Save all changes
        department = saveDepartmentDetailToRepository(department);
        if (!usersToSave.isEmpty()) {
            userRepository.saveAll(usersToSave); // Save all affected users at once
        }

        return department.getId();
    }

    private Department saveDepartmentDetailToRepository(Department department) {
        try {
            return departmentRepository.save(department);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to save data ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

}
