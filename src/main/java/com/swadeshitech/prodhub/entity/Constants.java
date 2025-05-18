package com.swadeshitech.prodhub.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "constants")
public class Constants extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String name;

    private List<String> values;
}
