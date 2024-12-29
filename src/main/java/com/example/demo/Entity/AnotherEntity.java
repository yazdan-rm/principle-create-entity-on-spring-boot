package com.example.demo.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "ANOTHER_ENTITY")
@SequenceGenerator(name = "another_entity_seq_gen", sequenceName = "another_entity_seq", allocationSize = 1)
public class AnotherEntity extends BaseEntity {

    @Column(name = "DESCRIPTION")
    private String description;

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
