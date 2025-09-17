package com.swadeshitech.prodhub.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "code_freeze")
public class CodeFreeze extends BaseEntity {

    @Id
    private String id;

    private String description;

    private boolean isActive;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @DBRef
    private transient List<User> approvers;

    @DBRef
    private List<Application> applications;
}
