package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team extends BaseEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator = "native")
    private Long id;

    private String name;

    private String description;

    private boolean isActive;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
        name = "team_user",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> employees = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
        name = "team_department",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<Department> departments = new HashSet<>();
}
