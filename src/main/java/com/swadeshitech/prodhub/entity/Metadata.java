package com.swadeshitech.prodhub.entity;

import java.io.Serializable;

import com.swadeshitech.prodhub.enums.ProfileType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Metadata extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator = "native")
    private Long id;
    
    private String name;

    @Enumerated(EnumType.STRING)
    private ProfileType profileType;

    @Column(columnDefinition  = "TEXT")
    private String data;

    private boolean isActive;

    @ManyToOne
    private Application application;
}
