package com.swadeshitech.prodhub.config;

import com.swadeshitech.prodhub.entity.ApprovalFlow;
import com.swadeshitech.prodhub.entity.FeatureFlag;
import com.swadeshitech.prodhub.repository.ApprovalFlowRepository;
import com.swadeshitech.prodhub.repository.FeatureFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeatureFlagInitializer implements CommandLineRunner {

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    @Autowired
    private ApprovalFlowRepository approvalFlowRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeFeatureFlags();
        initializeDefaultApprovalFlow();
    }

    private void initializeFeatureFlags() {
        // Initialize user_approval_required feature flag
        if (!featureFlagRepository.findByKey("user_approval_required").isPresent()) {
            FeatureFlag userApprovalFlag = FeatureFlag.builder()
                    .key("user_approval_required")
                    .name("User Approval Required")
                    .description("Require admin approval for new users before they can access the application")
                    .enabled(false)
                    .category("AUTH")
                    .defaultValue("false")
                    .dataType("BOOLEAN")
                    .build();
            featureFlagRepository.save(userApprovalFlag);
        }
    }

    private void initializeDefaultApprovalFlow() {
        // Check if default approval flow exists
        if (!approvalFlowRepository.findByIsDefaultTrue().isPresent()) {
            // Create default approval flow with basic stages
            ApprovalFlow.FlowStage qaStage = ApprovalFlow.FlowStage.builder()
                    .name("QA Approval")
                    .description("Quality Assurance team approval")
                    .sequence(1)
                    .isMandatory(true)
                    .minApprovalsRequired(1)
                    .requireAllApprovers(false)
                    .build();

            ApprovalFlow.FlowStage managerStage = ApprovalFlow.FlowStage.builder()
                    .name("Manager Approval")
                    .description("Manager approval required for deployment")
                    .sequence(2)
                    .isMandatory(true)
                    .minApprovalsRequired(1)
                    .requireAllApprovers(false)
                    .build();

            ApprovalFlow defaultFlow = ApprovalFlow.builder()
                    .key("default")
                    .name("Default Approval Flow")
                    .description("Default approval flow for deployments")
                    .isDefault(true)
                    .isActive(true)
                    .stages(List.of(qaStage, managerStage))
                    .build();

            approvalFlowRepository.save(defaultFlow);
        }
    }
}
