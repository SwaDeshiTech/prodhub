package com.swadeshitech.prodhub.services.approval;

import com.swadeshitech.prodhub.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ApprovalService {
    ApprovalResponse createApprovalRequest(ApprovalRequest request);
    ApprovalResponse getApprovalById(String requestId);
    boolean updateApprovalStatus(String requestId, ApprovalUpdateRequest request);
    PaginatedResponse<ApprovalResponse> getApprovalsList(ApprovalRequestFilter requestFilter, Integer page, Integer size, String sortBy, String order);
}
