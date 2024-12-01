package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class DepartmentResponse {
    
    private String id;
    private String name;
    private String description;
    private boolean isActive;

    @JsonFormat(pattern="yyyy-MM-dd hh:mm:ss")
    private LocalDateTime createTime;

    private String createdBy;
    private String modifiedBy;
}
