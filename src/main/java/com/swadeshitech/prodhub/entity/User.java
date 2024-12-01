package com.swadeshitech.prodhub.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator = "native")
    private Long id;

    private String name;

    private String phoneNumber;

    @Column(name = "uuid", unique = true)
    private String uuid;

    private String userName;

    @Column(unique = true)
    private String emailId;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive;

    private LocalDate dob;

    @ManyToMany
    private Set<Team> teams = new HashSet<>();

    @ManyToMany
    private Set<Department> departments = new HashSet<>();
}
