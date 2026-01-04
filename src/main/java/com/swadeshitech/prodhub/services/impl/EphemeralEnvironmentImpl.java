package com.swadeshitech.prodhub.services.impl;

import java.time.LocalDateTime;
import java.util.*;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.services.MetadataService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.swadeshitech.prodhub.enums.EphemeralEnvrionmentStatus;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.services.EphemeralEnvironmentService;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import lombok.extern.log4j.Log4j2;

import static com.swadeshitech.prodhub.constant.Constants.CLONE_METADATA_DELIMITER;

@Service
@Log4j2
public class EphemeralEnvironmentImpl implements EphemeralEnvironmentService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    MetadataService metadataService;

    @Override
    public EphemeralEnvironmentResponse createEphemeralEnvironment(EphemeralEnvironmentRequest request) {

        EphemeralEnvironment environment = modelMapper.map(request, EphemeralEnvironment.class);

        if (Objects.isNull(environment)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        String userId = UserContextUtil.getUserIdFromRequestContext();
        if (Objects.isNull(userId)) {
            log.error("user id is not present");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        environment.setStatus(EphemeralEnvrionmentStatus.CREATED);
        environment.setExpiryOn(LocalDateTime.now().plusDays(request.getExpiryDuration()));

        setOwnerDetail(environment, userId);
        setSharedWith(environment, request.getSharedWith());

        generateUpdatedProfiles(environment, request);

        writeTransactionService.saveEphemeralEnvironmentToRepository(environment);

        return modelMapper.map(environment, EphemeralEnvironmentResponse.class);
    }

    @Override
    public EphemeralEnvironmentResponse getEphemeralEnvironmentDetail(String id) {
        return mapEntityToResponse(fetchEphemeralEnvironmentFromDB(id));
    }

    private void setOwnerDetail(EphemeralEnvironment environment, String uuId) {

        Optional<User> user = userRepository.findByUuid(uuId);
        if (user.isEmpty()) {
            log.error("user could not be found", uuId);
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        environment.setOwner(user.get());
    }

    private void setSharedWith(EphemeralEnvironment environment, Set<String> users) {
        if(CollectionUtils.isEmpty(users)) {
            log.info("EphemeralEnvironment is not shared with anyone");
            return;
        }

        List<User> userList = new ArrayList<>();

        for(String user : users) {
            Optional<User> userOptional = userRepository.findByUuid(user);
            if (user.isEmpty()) {
                log.error("user could not be found", user);
                throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
            }
            userList.add(userOptional.get());
        }

        environment.setSharedWith(new HashSet<>(userList));
    }

    @Override
    public List<DropdownDTO> getEphemeralEnvironmentDropdownList() {

        List<EphemeralEnvironment> environments = readTransactionService.findByDynamicOrFilters(null, EphemeralEnvironment.class);
        if (environments.isEmpty()) {
            log.error("ephemeral environment list is empty");
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_LIST_NOT_FOUND);
        }

        List<DropdownDTO> dropdownDTOs = new ArrayList<>();

        for (EphemeralEnvironment environment : environments) {
            dropdownDTOs.add(DropdownDTO.builder().key(environment.getName()).value(environment.getId()).build());
        }

        return dropdownDTOs;
    }

    @Override
    public PaginatedResponse<EphemeralEnvironmentResponse> getEphemeralEnvironmentList(Integer page, Integer size, String sortBy, String order) {

        String userId = UserContextUtil.getUserIdFromRequestContext();
        if (Objects.isNull(userId)) {
            log.error("user id is not present");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        Optional<User> user = userRepository.findByUuid(userId);
        if (user.isEmpty()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Map<String, Object> filters = new HashMap<>();
        filters.put("owner", user.get());
        filters.put("sharedWith", user.get());

        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Page<EphemeralEnvironment> ephemeralEnvironmentPage = readTransactionService.findByDynamicOrFiltersPaginated(
                filters,
                EphemeralEnvironment.class,
                page,
                size,
                sortBy,
                direction
        );

        if (ephemeralEnvironmentPage.isEmpty()) {
            log.warn("No ephemeral environment found");
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_NOT_FOUND);
        }

        List<EphemeralEnvironmentResponse> dtoList = ephemeralEnvironmentPage.getContent().stream()
                .map(this::mapEntityToResponse)
                .toList();

        return PaginatedResponse.<EphemeralEnvironmentResponse>builder()
                .content(dtoList)
                .pageNumber(ephemeralEnvironmentPage.getNumber())
                .pageSize(ephemeralEnvironmentPage.getSize())
                .totalElements(ephemeralEnvironmentPage.getTotalElements())
                .totalPages(ephemeralEnvironmentPage.getTotalPages())
                .isLast(ephemeralEnvironmentPage.isLast())
                .build();
    }

    @Override
    public EphemeralEnvironmentResponse updateEphemeralEnvironment(String environmentId,
            EphemeralEnvironmentRequest request) {

        String userId = UserContextUtil.getUserIdFromRequestContext();
        if (Objects.isNull(userId)) {
            log.error("user id is not present");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        Optional<User> user = userRepository.findByUuid(userId);
        if (user.isEmpty()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<EphemeralEnvironment> environments = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", environmentId, "owner", user)
                , EphemeralEnvironment.class);

        if (environments.isEmpty()) {
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_NOT_FOUND);
        }

        EphemeralEnvironment ephemeralEnvironment = environments.getFirst();
        if (Objects.nonNull(request.getSharedWith())) {
            Set<User> users = new HashSet<>();
            for (String id : request.getSharedWith()) {
                Optional<User> sharedUserOpt = userRepository.findByUuid(id);
                if (sharedUserOpt.isEmpty()) {
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                }
                users.add(sharedUserOpt.get());
            }
            ephemeralEnvironment.setSharedWith(users);
        }

        generateUpdatedProfiles(ephemeralEnvironment, request);
        writeTransactionService.saveEphemeralEnvironmentToRepository(ephemeralEnvironment);

        return mapEntityToResponse(ephemeralEnvironment);
    }

    @Override
    public Map<String, Object> getMetadataFromEphemeralEnvironment(String id) {
        EphemeralEnvironment environment = fetchEphemeralEnvironmentFromDB(id);
        return null;//environment.getApplications();
    }

    @Override
    public void setUpProfiles(String ephemeralEnvironmentId, String profileType) {
        EphemeralEnvironment ephemeralEnvironment = fetchEphemeralEnvironmentFromDB(ephemeralEnvironmentId);


    }

    private EphemeralEnvironmentResponse mapEntityToResponse(EphemeralEnvironment environment) {

        EphemeralEnvironmentResponse environmentResponse = EphemeralEnvironmentResponse.builder()
                .owner(environment.getOwner().getName() + " (" + environment.getOwner().getEmailId() + ")")
                .status(environment.getStatus())
                .name(environment.getName())
                .id(environment.getId())
                .createdTime(environment.getCreatedTime())
                .expiryOn(environment.getExpiryOn())
                .build();

        if (environment.getAttachedProfiles() != null && !environment.getAttachedProfiles().isEmpty()) {
            Set<DropdownDTO> applicationsDropdownDTO = new HashSet<>();
            Map<String, Boolean> uniqueApplicationTrack = new HashMap<>();
            List<EphemeralEnvironmentResponse.EphemeralEnvironmentProfileResponse> profileResponses = new ArrayList<>();
            for (EphemeralEnvironment.Profile profile : environment.getAttachedProfiles()) {
                if(!uniqueApplicationTrack.containsKey(profile.getApplication().getName())) {
                    uniqueApplicationTrack.put(profile.getApplication().getName(), true);
                    applicationsDropdownDTO.add(DropdownDTO.builder()
                            .key(profile.getApplication().getName())
                            .value(profile.getApplication().getId())
                            .build());
                }
                profileResponses.add(EphemeralEnvironmentResponse.EphemeralEnvironmentProfileResponse.builder()
                                .applicationName(profile.getApplication().getName())
                                .buildProfileName(profile.getBuildProfile().getName().split(CLONE_METADATA_DELIMITER)[0])
                                .deploymentProfileName(profile.getDeploymentProfile().getName().split(CLONE_METADATA_DELIMITER)[0])
                                .buildProfileId(profile.getBuildProfile().getId())
                                .deploymentProfileId(profile.getDeploymentProfile().getId())
                        .build());
            }
            environmentResponse.setProfiles(profileResponses);
            environmentResponse.setApplications(applicationsDropdownDTO);
        }

        if (!CollectionUtils.isEmpty(environment.getSharedWith())) {
            Set<DropdownDTO> sharedWith = environment.getSharedWith().stream()
                    .map(user -> DropdownDTO.builder()
                            .key(user.getUuid())
                            .value(user.getEmailId())
                            .build())
                    .collect(java.util.stream.Collectors.toSet());
            environmentResponse.setSharedWith(sharedWith);
        }
        return environmentResponse;
    }

    @Override
    public EphemeralEnvironmentApplicationResponse getEphemeralEnvironmentApplicationDetails(String id,
            String applicationId) {

        String userId = UserContextUtil.getUserIdFromRequestContext();
        if (Objects.isNull(userId)) {
            log.error("user id is not present");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        Optional<User> user = userRepository.findByUuid(userId);
        if (user.isEmpty()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<EphemeralEnvironment> environments = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", id, "owner", user), EphemeralEnvironment.class);

        if (environments.isEmpty()) {
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_NOT_FOUND);
        }

        EphemeralEnvironment ephemeralEnvironment = environments.getFirst();
        EphemeralEnvironmentApplicationResponse response = new EphemeralEnvironmentApplicationResponse();
        response.setEphemeralEnvironmentName(ephemeralEnvironment.getName());

        /*Map<String, List<MetaDataResponse>> profileMetaData = new HashMap<>();
        for(EphemeralEnvironment.Profile profile : ephemeralEnvironment.getAttachedProfiles()) {
            if(profileMetaData.containsKey(profile.getApplication().getName())) {
                profileMetaData.get(profile.getApplication().getName()).add(MetaDataResponse.buildResponseObject(profile));
            } else {
                profileMetaData.put(profile.getApplication().getName(), List.of(MetaDataResponse.buildResponseObject(profile)));
            }
        }
        response.setApplications(profileMetaData);*/
        return response;
    }

    private EphemeralEnvironment fetchEphemeralEnvironmentFromDB(String id) {

        if (!StringUtils.hasText(id)) {
            log.error("ephemeral environment id is not present");
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_ID_NOT_FOUND);
        }

        List<EphemeralEnvironment> ephemeralEnvironments = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", new ObjectId(id)), EphemeralEnvironment.class
        );
        if (CollectionUtils.isEmpty(ephemeralEnvironments)) {
            log.error("Ephemeral envrionment could not be found {}", id);
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_NOT_FOUND);
        }

        return ephemeralEnvironments.getFirst();
    }

    private void generateUpdatedProfiles(EphemeralEnvironment ephemeralEnvironment, EphemeralEnvironmentRequest request) {
        if(request == null || request.getProfiles() == null || request.getProfiles().isEmpty()) {
            log.error("Profiles could not be found inside ephemeral environment request");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        List<EphemeralEnvironment.Profile> profiles = new ArrayList<>();
        for(EphemeralEnvironmentRequest.EphemeralEnvironmentRequestApplications application : request.getProfiles()) {
            switch(application.getActionType().toLowerCase()) {
                case "remove":
                    if(ephemeralEnvironment.getAttachedProfiles() != null && !ephemeralEnvironment.getAttachedProfiles().isEmpty()) {
                        for(EphemeralEnvironment.Profile profile : ephemeralEnvironment.getAttachedProfiles()) {
                            if(profile.getApplication().getId().equals(application.getApplicationId())
                                    && profile.getBuildProfile().getId().equals(application.getBuildProfileId())
                                    && profile.getDeploymentProfile().getId().equals(application.getDeploymentProfileId())) {
                                ephemeralEnvironment.getAttachedProfiles().remove(profile);
                                break;
                            }
                        }
                    }
                    log.info("Deployment profile {} and build profile {} of application {} has been removed", application.getDeploymentProfileId(), application.getBuildProfileId(), application.getApplicationId());
                    break;
                case "add":
                    List<Application> applications = readTransactionService.findApplicationByFilters(Map.of("_id", application.getApplicationId()));
                    if(applications == null || applications.isEmpty()) {
                        log.error("Failed to add application to ephemeral environment {}", application.getApplicationId());
                        throw new CustomException(ErrorCode.APPROVALS_NOT_FOUND);
                    }
                    List<Metadata> deploymentProfileList = readTransactionService.findMetaDataByFilters(Map.of("_id", application.getDeploymentProfileId()));
                    if(deploymentProfileList == null || deploymentProfileList.isEmpty() ) {
                        log.error("Failed to add deployment profile id {}", application.getDeploymentProfileId());
                        throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
                    }
                    Metadata deploymentProfile = deploymentProfileList.getFirst();
                    String clonedDeploymentProfileName = deploymentProfile.getName() + CLONE_METADATA_DELIMITER + deploymentProfile.getProfileType().name() + CLONE_METADATA_DELIMITER + ephemeralEnvironment.getName();
                    Metadata clonedDeploymentProfile = metadataService.cloneProfile(deploymentProfile.getId(), clonedDeploymentProfileName);

                    List<Metadata> buildProfileList = readTransactionService.findMetaDataByFilters(Map.of("_id", application.getBuildProfileId()));
                    if(buildProfileList == null || buildProfileList.isEmpty()) {
                        log.error("Failed to add build profile id {}", application.getBuildProfileId());
                        throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
                    }
                    Metadata buildProfile = buildProfileList.getFirst();
                    String clonedBuildProfileName = buildProfile.getName() + CLONE_METADATA_DELIMITER + buildProfile.getProfileType().name() + CLONE_METADATA_DELIMITER + ephemeralEnvironment.getName();
                    Metadata clonedBuildProfile = metadataService.cloneProfile(buildProfile.getId(), clonedBuildProfileName);

                    profiles.add(EphemeralEnvironment.Profile.builder()
                            .application(applications.getFirst())
                            .buildProfile(clonedBuildProfile)
                            .deploymentProfile(clonedDeploymentProfile)
                            .build());
            }
        }
        if(ephemeralEnvironment.getAttachedProfiles() != null && !ephemeralEnvironment.getAttachedProfiles().isEmpty()) {
            profiles.addAll(ephemeralEnvironment.getAttachedProfiles());
        }
        ephemeralEnvironment.setAttachedProfiles(profiles);
    }
}
