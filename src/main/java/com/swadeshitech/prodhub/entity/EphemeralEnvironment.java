package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ephemeralEnvironments")
public class EphemeralEnvironment extends BaseEntity implements Serializable {

    @Id
    private String name;

    private String description;

    private LocalDateTime expiryOn;

    private boolean isActive;

    @DBRef
    private User owner;

    @DBRef
    private Set<User> sharedWith;

    @DBRef
    private Set<Application> applications;
}