package com.swadeshitech.prodhub.transaction.write;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.BuildProvider;
import com.swadeshitech.prodhub.entity.CloudProvider;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.Organization;
import com.swadeshitech.prodhub.entity.ResourceDetails;
import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.entity.SCM;
import com.swadeshitech.prodhub.entity.Tab;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ApplicationRepository;
import com.swadeshitech.prodhub.repository.BuildProviderRepository;
import com.swadeshitech.prodhub.repository.CloudProviderRepository;
import com.swadeshitech.prodhub.repository.MetaDataRepository;
import com.swadeshitech.prodhub.repository.OrganizationRepository;
import com.swadeshitech.prodhub.repository.ResourceRepository;
import com.swadeshitech.prodhub.repository.RoleRepository;
import com.swadeshitech.prodhub.repository.SCMRepository;
import com.swadeshitech.prodhub.repository.TabRepository;
import com.swadeshitech.prodhub.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WriteTransactionService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MetaDataRepository metaDataRepository;

    @Autowired
    private CloudProviderRepository cloudProviderRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TabRepository tabRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SCMRepository scmRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private BuildProviderRepository buildProviderRepository;

    public Application saveApplicationToRepository(Application application) {
        try {
            return applicationRepository.save(application);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to save data ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public Metadata saveMetaDataToRepository(Metadata metadata) {
        try {
            return metaDataRepository.save(metadata);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update metadata ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public CloudProvider saveCloudProviderToRepository(CloudProvider cloudProvider) {
        try {
            return cloudProviderRepository.save(cloudProvider);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update metadata ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public ResourceDetails saveResourceDetailsToRepository(ResourceDetails resourceDetails) {
        try {
            return resourceRepository.save(resourceDetails);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update metadata ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public void removeCloudProviderFromRepository(String id) {
        try {
            cloudProviderRepository.deleteById(id);
        } catch (Exception ex) {
            log.error("Failed to delete CloudProvider with id: {}", id, ex);
            throw new CustomException(ErrorCode.CLOUD_PROVIDER_COULD_NOT_DELETED);
        }
    }

    public Role saveRoleToRepository(Role role) {
        try {
            return roleRepository.save(role);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update metadata ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public Tab saveTabToRepository(Tab tab) {
        try {
            return tabRepository.save(tab);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update metadata ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public User saveUserToRepository(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update user ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public SCM saveSCMToRepository(SCM scm) {
        try {
            return scmRepository.save(scm);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update SCM ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public void removeSCMFromRepository(String id) {
        try {
            scmRepository.deleteById(id);
        } catch (Exception ex) {
            log.error("Failed to delete SCM with id: {}", id, ex);
            throw new CustomException(ErrorCode.SCM_COULD_NOT_BE_DELETED);
        }
    }

    public Organization saveOrganizationToRepository(Organization organization) {
        try {
            return organizationRepository.save(organization);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update organization ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public void removeOrganizationFromRepository(String id) {
        try {
            organizationRepository.deleteById(id);
        } catch (Exception ex) {
            log.error("Failed to delete Organization with id: {}", id, ex);
            throw new CustomException(ErrorCode.ORGANIZATION_COULD_NOT_BE_DELETED);
        }
    }

    public BuildProvider saveBuildProviderToRepository(BuildProvider buildProvider) {
        try {
            return buildProviderRepository.save(buildProvider);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update Build Provider ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public void removeBuildProviderFromRepository(String id) {
        try {
            buildProviderRepository.deleteById(id);
        } catch (Exception ex) {
            log.error("Failed to delete Build Provider with id: {}", id, ex);
            throw new CustomException(ErrorCode.BUILD_PROVIDER_COULD_NOT_BE_DELETED);
        }
    }
}
