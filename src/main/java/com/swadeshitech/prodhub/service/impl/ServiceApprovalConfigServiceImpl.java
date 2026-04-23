package com.swadeshitech.prodhub.service.impl;

import com.swadeshitech.prodhub.entity.ApprovalFlow;
import com.swadeshitech.prodhub.entity.ServiceApprovalConfig;
import com.swadeshitech.prodhub.repository.ServiceApprovalConfigRepository;
import com.swadeshitech.prodhub.service.ApprovalFlowService;
import com.swadeshitech.prodhub.service.ServiceApprovalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ServiceApprovalConfigServiceImpl implements ServiceApprovalConfigService {

    @Autowired
    private ServiceApprovalConfigRepository serviceApprovalConfigRepository;

    @Autowired
    private ApprovalFlowService approvalFlowService;

    @Override
    public ServiceApprovalConfig createServiceApprovalConfig(ServiceApprovalConfig config) {
        return serviceApprovalConfigRepository.save(config);
    }

    @Override
    public ServiceApprovalConfig updateServiceApprovalConfig(String id, ServiceApprovalConfig config) {
        config.setId(id);
        config.setUpdatedAt(LocalDateTime.now());
        return serviceApprovalConfigRepository.save(config);
    }

    @Override
    public void deleteServiceApprovalConfig(String serviceId) {
        Optional<ServiceApprovalConfig> configOpt = serviceApprovalConfigRepository.findByServiceId(serviceId);
        if (configOpt.isPresent()) {
            serviceApprovalConfigRepository.delete(configOpt.get());
        }
    }

    @Override
    public Optional<ServiceApprovalConfig> getServiceApprovalConfig(String serviceId) {
        return serviceApprovalConfigRepository.findByServiceId(serviceId);
    }

    @Override
    public ApprovalFlow getApprovalFlowForService(String serviceId) {
        Optional<ServiceApprovalConfig> configOpt = serviceApprovalConfigRepository.findByServiceId(serviceId);
        if (configOpt.isPresent()) {
            ServiceApprovalConfig config = configOpt.get();
            if (config.isUseCustomFlow() && config.getApprovalFlow() != null) {
                return config.getApprovalFlow();
            }
        }
        return null;
    }

    @Override
    public ApprovalFlow getApprovalFlowForServiceWithFallback(String serviceId) {
        // First try to get custom flow for the service
        ApprovalFlow customFlow = getApprovalFlowForService(serviceId);
        if (customFlow != null) {
            return customFlow;
        }

        // Fallback to default flow
        Optional<ApprovalFlow> defaultFlow = approvalFlowService.getDefaultApprovalFlow();
        return defaultFlow.orElse(null);
    }

    @Override
    public ServiceApprovalConfig setCustomApprovalFlow(String serviceId, String approvalFlowId) {
        Optional<ServiceApprovalConfig> configOpt = serviceApprovalConfigRepository.findByServiceId(serviceId);
        ServiceApprovalConfig config;

        if (configOpt.isPresent()) {
            config = configOpt.get();
        } else {
            config = ServiceApprovalConfig.builder()
                    .serviceId(serviceId)
                    .useCustomFlow(false)
                    .build();
        }

        Optional<ApprovalFlow> flowOpt = approvalFlowService.getApprovalFlowById(approvalFlowId);
        if (!flowOpt.isPresent()) {
            throw new RuntimeException("Approval flow not found with id: " + approvalFlowId);
        }

        config.setApprovalFlow(flowOpt.get());
        config.setUseCustomFlow(true);
        config.setOverrideDefault(true);
        config.setUpdatedAt(LocalDateTime.now());

        return serviceApprovalConfigRepository.save(config);
    }

    @Override
    public ServiceApprovalConfig clearCustomApprovalFlow(String serviceId) {
        Optional<ServiceApprovalConfig> configOpt = serviceApprovalConfigRepository.findByServiceId(serviceId);
        if (!configOpt.isPresent()) {
            throw new RuntimeException("Service approval config not found for service id: " + serviceId);
        }

        ServiceApprovalConfig config = configOpt.get();
        config.setUseCustomFlow(false);
        config.setApprovalFlow(null);
        config.setOverrideDefault(false);
        config.setUpdatedAt(LocalDateTime.now());

        return serviceApprovalConfigRepository.save(config);
    }
}
