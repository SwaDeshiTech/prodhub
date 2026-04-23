package com.swadeshitech.prodhub.service.impl;

import com.swadeshitech.prodhub.entity.ApprovalFlow;
import com.swadeshitech.prodhub.repository.ApprovalFlowRepository;
import com.swadeshitech.prodhub.service.ApprovalFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApprovalFlowServiceImpl implements ApprovalFlowService {

    @Autowired
    private ApprovalFlowRepository approvalFlowRepository;

    @Override
    public ApprovalFlow createApprovalFlow(ApprovalFlow approvalFlow) {
        return approvalFlowRepository.save(approvalFlow);
    }

    @Override
    public ApprovalFlow updateApprovalFlow(String id, ApprovalFlow approvalFlow) {
        approvalFlow.setId(id);
        approvalFlow.setUpdatedAt(LocalDateTime.now());
        return approvalFlowRepository.save(approvalFlow);
    }

    @Override
    public void deleteApprovalFlow(String id) {
        approvalFlowRepository.deleteById(id);
    }

    @Override
    public Optional<ApprovalFlow> getApprovalFlowById(String id) {
        return approvalFlowRepository.findById(id);
    }

    @Override
    public Optional<ApprovalFlow> getApprovalFlowByKey(String key) {
        return approvalFlowRepository.findByKey(key);
    }

    @Override
    public List<ApprovalFlow> getAllApprovalFlows() {
        return approvalFlowRepository.findAll();
    }

    @Override
    public List<ApprovalFlow> getActiveApprovalFlows() {
        return approvalFlowRepository.findByIsActive(true);
    }

    @Override
    public Optional<ApprovalFlow> getDefaultApprovalFlow() {
        return approvalFlowRepository.findByIsDefaultTrue();
    }

    @Override
    public ApprovalFlow setAsDefault(String id) {
        // Remove default from all other flows
        List<ApprovalFlow> allFlows = approvalFlowRepository.findAll();
        allFlows.forEach(flow -> {
            if (flow.isDefault()) {
                flow.setDefault(false);
                approvalFlowRepository.save(flow);
            }
        });

        // Set the new default
        Optional<ApprovalFlow> flowOpt = approvalFlowRepository.findById(id);
        if (!flowOpt.isPresent()) {
            throw new RuntimeException("Approval flow not found with id: " + id);
        }

        ApprovalFlow flow = flowOpt.get();
        flow.setDefault(true);
        flow.setUpdatedAt(LocalDateTime.now());
        return approvalFlowRepository.save(flow);
    }

    @Override
    public ApprovalFlow toggleActive(String id) {
        Optional<ApprovalFlow> flowOpt = approvalFlowRepository.findById(id);
        if (!flowOpt.isPresent()) {
            throw new RuntimeException("Approval flow not found with id: " + id);
        }

        ApprovalFlow flow = flowOpt.get();
        flow.setActive(!flow.isActive());
        flow.setUpdatedAt(LocalDateTime.now());
        return approvalFlowRepository.save(flow);
    }
}
