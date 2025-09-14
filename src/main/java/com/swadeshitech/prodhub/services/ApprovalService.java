package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.ApprovalRequest;
import com.swadeshitech.prodhub.dto.ApprovalRequestFilter;
import com.swadeshitech.prodhub.dto.ApprovalResponse;
import com.swadeshitech.prodhub.dto.ApprovalUpdateRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ApprovalService {
    public ApprovalResponse createApprovalRequest(ApprovalRequest request);
    public ApprovalResponse getApprovalById(String requestId);
    public boolean updateApprovalStatus(String requestId, ApprovalUpdateRequest request);
    public List<ApprovalResponse> getApprovalsList(ApprovalRequestFilter requestFilter);
}
