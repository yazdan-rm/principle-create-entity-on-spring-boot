package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@MappedSuperclass
public abstract class BaseEntity {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE) // Strategy defined in child classes
    private Long id;

    @Setter
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Setter
    @UpdateTimestamp
    private LocalDateTime updatedDate;

    @Version
    private Long version;

    public Long id() {
        return id;
    }

    public LocalDateTime createdDate() {
        return createdDate;
    }

    public LocalDateTime updatedDate() {
        return updatedDate;
    }

}
