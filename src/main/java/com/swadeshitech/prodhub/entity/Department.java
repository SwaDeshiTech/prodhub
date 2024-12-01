package com.swadeshitech.prodhub.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "uuid", unique = true)
    private String uuid;

    private String name;
    
    private String description;

    private boolean isActive;
}
