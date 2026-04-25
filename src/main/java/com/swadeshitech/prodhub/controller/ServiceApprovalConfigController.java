package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.entity.ApprovalFlow;
import com.swadeshitech.prodhub.entity.ServiceApprovalConfig;
import com.swadeshitech.prodhub.service.ServiceApprovalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/service-approval-configs")
public class ServiceApprovalConfigController {

    @Autowired
    private ServiceApprovalConfigService serviceApprovalConfigService;

    @PostMapping
    public ResponseEntity<ServiceApprovalConfig> createServiceApprovalConfig(
            @RequestBody ServiceApprovalConfig config) {
        ServiceApprovalConfig created = serviceApprovalConfigService.createServiceApprovalConfig(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceApprovalConfig> updateServiceApprovalConfig(
            @PathVariable String id,
            @RequestBody ServiceApprovalConfig config) {
        try {
            ServiceApprovalConfig updated = serviceApprovalConfigService.updateServiceApprovalConfig(id, config);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteServiceApprovalConfig(@PathVariable String serviceId) {
        serviceApprovalConfigService.deleteServiceApprovalConfig(serviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceApprovalConfig> getServiceApprovalConfig(@PathVariable String serviceId) {
        return serviceApprovalConfigService.getServiceApprovalConfig(serviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{serviceId}/approval-flow")
    public ResponseEntity<ApprovalFlow> getApprovalFlowForService(@PathVariable String serviceId) {
        ApprovalFlow flow = serviceApprovalConfigService.getApprovalFlowForService(serviceId);
        if (flow != null) {
            return ResponseEntity.ok(flow);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{serviceId}/approval-flow/with-fallback")
    public ResponseEntity<ApprovalFlow> getApprovalFlowForServiceWithFallback(@PathVariable String serviceId) {
        ApprovalFlow flow = serviceApprovalConfigService.getApprovalFlowForServiceWithFallback(serviceId);
        if (flow != null) {
            return ResponseEntity.ok(flow);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{serviceId}/set-custom-flow")
    public ResponseEntity<ServiceApprovalConfig> setCustomApprovalFlow(
            @PathVariable String serviceId,
            @RequestBody Map<String, String> request) {
        try {
            String approvalFlowId = request.get("approvalFlowId");
            ServiceApprovalConfig config = serviceApprovalConfigService.setCustomApprovalFlow(serviceId, approvalFlowId);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{serviceId}/clear-custom-flow")
    public ResponseEntity<ServiceApprovalConfig> clearCustomApprovalFlow(@PathVariable String serviceId) {
        try {
            ServiceApprovalConfig config = serviceApprovalConfigService.clearCustomApprovalFlow(serviceId);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
