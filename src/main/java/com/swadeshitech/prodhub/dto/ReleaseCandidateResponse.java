package com.swadeshitech.prodhub.dto;

import java.util.Map;

import com.swadeshitech.prodhub.enums.ReleaseCandidateStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseCandidateResponse extends BaseResponse {
    private String id;
    private Map<String, String> metaData;
    private ReleaseCandidateStatus status;
    private String certifiedBy;
    private String initiatedBy;
    private String serviceName;
    private String buildProfile;
}
