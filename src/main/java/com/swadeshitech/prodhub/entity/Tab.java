package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "tabs")
@EqualsAndHashCode(callSuper = true)
public class Tab extends BaseEntity implements Serializable {

    @Id
    private String id;

    private String name;

    private boolean isActive;

    private String link;

    private Set<Tab> children;

    private Set<Role> roles;
}
