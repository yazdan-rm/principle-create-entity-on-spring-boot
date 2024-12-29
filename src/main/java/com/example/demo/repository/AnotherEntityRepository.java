package com.example.demo.repository;

import com.example.demo.Entity.AnotherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnotherEntityRepository extends JpaRepository<AnotherEntity, Long> {
}
