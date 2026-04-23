package com.swadeshitech.prodhub.service;

import com.swadeshitech.prodhub.entity.ApprovalFlow;
import com.swadeshitech.prodhub.entity.ServiceApprovalConfig;

import java.util.Optional;

public interface ServiceApprovalConfigService {
    ServiceApprovalConfig createServiceApprovalConfig(ServiceApprovalConfig config);
    ServiceApprovalConfig updateServiceApprovalConfig(String id, ServiceApprovalConfig config);
    void deleteServiceApprovalConfig(String serviceId);
    Optional<ServiceApprovalConfig> getServiceApprovalConfig(String serviceId);
    ApprovalFlow getApprovalFlowForService(String serviceId);
    ApprovalFlow getApprovalFlowForServiceWithFallback(String serviceId);
    ServiceApprovalConfig setCustomApprovalFlow(String serviceId, String approvalFlowId);
    ServiceApprovalConfig clearCustomApprovalFlow(String serviceId);
}
