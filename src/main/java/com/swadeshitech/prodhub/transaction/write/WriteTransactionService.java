package com.swadeshitech.prodhub.transaction.write;

import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WriteTransactionService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MetaDataRepository metaDataRepository;

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
    private ReleaseCandidateRepository releaseCandidateRepository;

    @Autowired
    private MetaDataRepository metadataRepository;

    @Autowired
    private CredentialProviderRepository credentialProviderRepository;

    @Autowired
    private ApprovalsRepository approvalsRepository;

    @Autowired
    private CodeFreezeRepository codeFreezeRepository;

    @Autowired
    private DeploymentSetRepository deploymentSetRepository;

    @Autowired
    private ApprovalStageRepository approvalStageRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Autowired
    private DeploymentRunRepository deploymentRunRepository;

    @Autowired
    private DeploymentTemplateRepository deploymentTemplateRepository;

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

    public ReleaseCandidate saveReleaseCandidateToRepository(ReleaseCandidate releaseCandidate) {
        try {
            return releaseCandidateRepository.save(releaseCandidate);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update Release Candidate ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    public void removeReleaseCandidateFromRepository(String id) {
        try {
            releaseCandidateRepository.deleteById(id);
        } catch (Exception ex) {
            log.error("Failed to delete Release Candidate with id: {}", id, ex);
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_COULD_NOT_BE_DELETED);
        }
    }

    public Metadata saveMetadataToRepository(Metadata metadata) {
        try {
            return metadataRepository.save(metadata);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update Metadata ", ex);
            throw new CustomException(ErrorCode.METADATA_PROFILE_UPDATE_FAILED);
        }
    }

    public CredentialProvider saveCredentialProviderToRepository(CredentialProvider credentialProvider) {
        try {
            return credentialProviderRepository.save(credentialProvider);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update Metadata ", ex);
            throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_COULD_NOT_BE_UPDATED);
        }
    }

    public Approvals saveApprovalsToRepository(Approvals approvals) {
        try {
            return approvalsRepository.save(approvals);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update approval repository ", ex);
            throw new CustomException(ErrorCode.APPROVALS_UPDATE_FAILED);
        }
    }

    public ApprovalStage saveApprovalStageToRepository(ApprovalStage approvalStage) {
        try {
            return approvalStageRepository.save(approvalStage);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update approval stage ", ex);
            throw new CustomException(ErrorCode.APPROVALS_STAGE_UPDATE_FAILED);
        }
    }

    public CodeFreeze saveCodeFreezeToRepository(CodeFreeze codeFreeze) {
        try {
            return codeFreezeRepository.save(codeFreeze);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update Metadata ", ex);
            throw new CustomException(ErrorCode.CODE_FREEZE_UPDATE_FAILED);
        }
    }

    public DeploymentSet saveDeploymentSetToRepository(DeploymentSet deploymentSet) {
        try {
            return deploymentSetRepository.save(deploymentSet);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update deployment set ", ex);
            throw new CustomException(ErrorCode.DEPLOYMENT_SET_COULD_NOT_BE_CREATED);
        }
    }

    public Deployment saveDeploymentToRepository(Deployment deployment) {
        try {
            return deploymentRepository.save(deployment);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update deployment ", ex);
            throw new CustomException(ErrorCode.DEPLOYMENT_COULD_NOT_BE_CREATED);
        }
    }

    public DeploymentRun saveDeploymentRunToRepository(DeploymentRun deploymentRun) {
        try {
            return deploymentRunRepository.save(deploymentRun);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update deployment run ", ex);
            throw new CustomException(ErrorCode.DEPLOYMENT_RUN_COULD_NOT_BE_CREATED);
        }
    }

    public DeploymentTemplate saveDeploymentTemplate(DeploymentTemplate deploymentTemplate) {
        try {
            return deploymentTemplateRepository.save(deploymentTemplate);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to update deployment run ", ex);
            throw new CustomException(ErrorCode.DEPLOYMENT_TEMPLATE_COULD_NOT_BE_CREATED);
        }
    }
}
