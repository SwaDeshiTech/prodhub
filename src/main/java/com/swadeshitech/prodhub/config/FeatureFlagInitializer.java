package com.swadeshitech.prodhub.config;

import com.swadeshitech.prodhub.entity.FeatureFlag;
import com.swadeshitech.prodhub.repository.FeatureFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlagInitializer implements CommandLineRunner {

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeFeatureFlags();
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
}
