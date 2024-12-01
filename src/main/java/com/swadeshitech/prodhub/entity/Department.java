package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class Department extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO, generator = "native")
    private Long id;

    private String name;
    
    private String description;

    private boolean isActive;

    @ManyToMany
    private Set<Team> teams = new HashSet<>();

    @ManyToMany
    private Set<User> users = new HashSet<>();
}
