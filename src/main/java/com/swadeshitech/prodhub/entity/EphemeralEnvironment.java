package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class EphemeralEnvironment extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator = "native")
    private Long id;
    
    private String name;

    private String description;

    private LocalDateTime expiryOn;

    private boolean isActive;

    private User owner;

    @ManyToMany
    private Set<User> sharedWith;

    @ManyToMany
    private Set<Application> applications;
}