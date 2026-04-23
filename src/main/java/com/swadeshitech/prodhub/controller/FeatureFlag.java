package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.entity.FeatureFlag;
import com.swadeshitech.prodhub.service.FeatureFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feature-flags")
@CrossOrigin(origins = "*")
public class FeatureFlagController {

    @Autowired
    private FeatureFlagService featureFlagService;

    @GetMapping
    public ResponseEntity<List<FeatureFlag>> getAllFeatureFlags() {
        List<FeatureFlag> flags = featureFlagService.getAllFeatureFlags();
        return ResponseEntity.ok(flags);
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<FeatureFlag> getFeatureFlagByKey(@PathVariable String key) {
        Optional<FeatureFlag> flag = featureFlagService.getFeatureFlagByKey(key);
        return flag.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/enabled/{key}")
    public ResponseEntity<Boolean> isFeatureEnabled(@PathVariable String key) {
        boolean enabled = featureFlagService.isFeatureEnabled(key);
        return ResponseEntity.ok(enabled);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeatureFlag> getFeatureFlagById(@PathVariable String id) {
        Optional<FeatureFlag> flag = featureFlagService.getFeatureFlagById(id);
        return flag.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FeatureFlag> createFeatureFlag(@RequestBody FeatureFlag featureFlag) {
        FeatureFlag createdFlag = featureFlagService.createFeatureFlag(featureFlag);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFlag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureFlag> updateFeatureFlag(@PathVariable String id, @RequestBody FeatureFlag featureFlag) {
        try {
            FeatureFlag updatedFlag = featureFlagService.updateFeatureFlag(id, featureFlag);
            return ResponseEntity.ok(updatedFlag);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<FeatureFlag> toggleFeatureFlag(@PathVariable String id) {
        try {
            FeatureFlag flag = featureFlagService.getFeatureFlagById(id)
                    .orElseThrow(() -> new RuntimeException("Feature flag not found"));
            FeatureFlag toggledFlag = featureFlagService.toggleFeatureFlag(flag.getKey());
            return ResponseEntity.ok(toggledFlag);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeatureFlag(@PathVariable String id) {
        try {
            featureFlagService.deleteFeatureFlag(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
