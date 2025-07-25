package com.swadeshitech.prodhub.transaction.write;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.CloudProvider;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.ResourceDetails;
import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ApplicationRepository;
import com.swadeshitech.prodhub.repository.CloudProviderRepository;
import com.swadeshitech.prodhub.repository.MetaDataRepository;
import com.swadeshitech.prodhub.repository.ResourceRepository;
import com.swadeshitech.prodhub.repository.RoleRepository;

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
}
