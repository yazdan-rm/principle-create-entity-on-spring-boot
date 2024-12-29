package com.example.demo.controller;

import com.example.demo.Entity.AnotherEntity;
import com.example.demo.repository.AnotherEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class AnotherEntityController {

    private final AnotherEntityRepository anotherEntityRepository;

    public AnotherEntityController(AnotherEntityRepository anotherEntityRepository) {
        this.anotherEntityRepository = anotherEntityRepository;
    }

    @PostMapping("/another-entity")
    public ResponseEntity<AnotherEntity> save(@RequestBody AnotherEntity anotherEntity) {
        return ResponseEntity.ok(anotherEntityRepository.saveAndFlush(anotherEntity));
    }

    @PutMapping("/another-entity/{id}")
    public ResponseEntity<AnotherEntity> update(@PathVariable Long id, @RequestBody AnotherEntity anotherEntity) {
        var fetchRequestedMyEntity = anotherEntityRepository.findById(id);
        if(fetchRequestedMyEntity.isPresent()) {
            AnotherEntity updatedAnotherEntity = fetchRequestedMyEntity.get();
            updatedAnotherEntity.setDescription(anotherEntity.getDescription());
            return ResponseEntity.ok(anotherEntityRepository.save(updatedAnotherEntity));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("another-entity")
    public ResponseEntity<List<AnotherEntity>> findAll() {
        return ResponseEntity.ok(anotherEntityRepository.findAll());
    }
}
