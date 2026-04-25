package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.entity.ApprovalFlow;
import com.swadeshitech.prodhub.service.ApprovalFlowService;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/approval-flows")
public class ApprovalFlowController {

    @Autowired
    private ApprovalFlowService approvalFlowService;

    @PostMapping
    public ResponseEntity<ApprovalFlow> createApprovalFlow(@RequestBody ApprovalFlow approvalFlow) {
        ApprovalFlow created = approvalFlowService.createApprovalFlow(approvalFlow);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovalFlow> updateApprovalFlow(
            @PathVariable String id,
            @RequestBody ApprovalFlow approvalFlow) {
        try {
            ApprovalFlow updated = approvalFlowService.updateApprovalFlow(id, approvalFlow);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApprovalFlow(@PathVariable String id) {
        try {
            approvalFlowService.deleteApprovalFlow(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalFlow> getApprovalFlowById(@PathVariable String id) {
        return approvalFlowService.getApprovalFlowById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<ApprovalFlow> getApprovalFlowByKey(@PathVariable String key) {
        return approvalFlowService.getApprovalFlowByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ApprovalFlow>> getAllApprovalFlows() {
        List<ApprovalFlow> flows = approvalFlowService.getAllApprovalFlows();
        return ResponseEntity.ok(flows);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ApprovalFlow>> getActiveApprovalFlows() {
        List<ApprovalFlow> flows = approvalFlowService.getActiveApprovalFlows();
        return ResponseEntity.ok(flows);
    }

    @GetMapping("/default")
    public ResponseEntity<ApprovalFlow> getDefaultApprovalFlow() {
        return approvalFlowService.getDefaultApprovalFlow()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/set-default")
    public ResponseEntity<ApprovalFlow> setAsDefault(@PathVariable String id) {
        try {
            ApprovalFlow flow = approvalFlowService.setAsDefault(id);
            return ResponseEntity.ok(flow);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/toggle-active")
    public ResponseEntity<ApprovalFlow> toggleActive(@PathVariable String id) {
        try {
            ApprovalFlow flow = approvalFlowService.toggleActive(id);
            return ResponseEntity.ok(flow);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
