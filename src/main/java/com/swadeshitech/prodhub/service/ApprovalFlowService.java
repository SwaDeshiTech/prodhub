package com.swadeshitech.prodhub.service;

import com.swadeshitech.prodhub.entity.ApprovalFlow;

import java.util.List;
import java.util.Optional;

public interface ApprovalFlowService {
    ApprovalFlow createApprovalFlow(ApprovalFlow approvalFlow);
    ApprovalFlow updateApprovalFlow(String id, ApprovalFlow approvalFlow);
    void deleteApprovalFlow(String id);
    Optional<ApprovalFlow> getApprovalFlowById(String id);
    Optional<ApprovalFlow> getApprovalFlowByKey(String key);
    List<ApprovalFlow> getAllApprovalFlows();
    List<ApprovalFlow> getActiveApprovalFlows();
    Optional<ApprovalFlow> getDefaultApprovalFlow();
    ApprovalFlow setAsDefault(String id);
    ApprovalFlow toggleActive(String id);
}
