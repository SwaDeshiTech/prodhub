package com.swadeshitech.prodhub.entity;

import java.io.Serializable;

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
@Document(collection = "roles")
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity implements Serializable {

    @Id
    private String id;

    private String name;

    private boolean isActive;

    private boolean isDefault;
}
