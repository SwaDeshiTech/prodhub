package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Application extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator = "native")
    private Long id;
    
    private String name;

    private String description;

    @ManyToOne
    private Team team;

    @ManyToOne
    private Department department;

    private boolean isActive;

    @OneToMany(cascade = CascadeType.PERSIST)
    private Set<Metadata> profiles;
}
