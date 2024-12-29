package com.example.demo.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Setter;


@Setter
@Entity
@Table(name = "my_entity")
@SequenceGenerator(name = "my_entity_seq_gen", sequenceName = "my_entity_seq", allocationSize = 1)
public class MyEntity extends BaseEntity {

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

}
