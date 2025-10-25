package com.swadeshitech.prodhub.services.approval;

import com.swadeshitech.prodhub.dto.ApprovalRequest;
import com.swadeshitech.prodhub.dto.ApprovalRequestFilter;
import com.swadeshitech.prodhub.dto.ApprovalResponse;
import com.swadeshitech.prodhub.dto.ApprovalUpdateRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ApprovalService {
    ApprovalResponse createApprovalRequest(ApprovalRequest request);
    ApprovalResponse getApprovalById(String requestId);
    boolean updateApprovalStatus(String requestId, ApprovalUpdateRequest request);
    List<ApprovalResponse> getApprovalsList(ApprovalRequestFilter requestFilter);
}
