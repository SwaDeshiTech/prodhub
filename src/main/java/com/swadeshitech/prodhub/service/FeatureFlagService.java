package com.swadeshitech.prodhub.service;

import com.swadeshitech.prodhub.entity.FeatureFlag;
import com.swadeshitech.prodhub.repository.FeatureFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeatureFlagService {

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    public List<FeatureFlag> getAllFeatureFlags() {
        return featureFlagRepository.findAll();
    }

    public Optional<FeatureFlag> getFeatureFlagById(String id) {
        return featureFlagRepository.findById(id);
    }

    public Optional<FeatureFlag> getFeatureFlagByKey(String key) {
        return featureFlagRepository.findByKey(key);
    }

    public boolean isFeatureEnabled(String key) {
        Optional<FeatureFlag> flag = featureFlagRepository.findByKey(key);
        return flag.map(FeatureFlag::isEnabled).orElse(false);
    }

    public FeatureFlag createFeatureFlag(FeatureFlag featureFlag) {
        return featureFlagRepository.save(featureFlag);
    }

    public FeatureFlag updateFeatureFlag(String id, FeatureFlag featureFlag) {
        featureFlag.setId(id);
        return featureFlagRepository.save(featureFlag);
    }

    public void deleteFeatureFlag(String id) {
        featureFlagRepository.deleteById(id);
    }

    public FeatureFlag toggleFeatureFlag(String key) {
        Optional<FeatureFlag> flag = featureFlagRepository.findByKey(key);
        if (flag.isPresent()) {
            FeatureFlag existingFlag = flag.get();
            existingFlag.setEnabled(!existingFlag.isEnabled());
            return featureFlagRepository.save(existingFlag);
        }
        throw new RuntimeException("Feature flag not found: " + key);
    }
}
