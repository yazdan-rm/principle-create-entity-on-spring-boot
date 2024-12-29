package com.example.demo.controller;

import com.example.demo.Entity.MyEntity;
import com.example.demo.repository.MyEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class MyEntityController {

    private final MyEntityRepository myEntityRepository;

    public MyEntityController(MyEntityRepository myEntityRepository) {
        this.myEntityRepository = myEntityRepository;
    }

    @PostMapping("/my-entity")
    public ResponseEntity<MyEntity> save(@RequestBody MyEntity myEntity) {
        return ResponseEntity.ok(myEntityRepository.saveAndFlush(myEntity));
    }

    @PutMapping("/my-entity/{id}")
    public ResponseEntity<MyEntity> update(@PathVariable Long id, @RequestBody MyEntity myEntity) {
        var fetchRequestedMyEntity = myEntityRepository.findById(id);
        if(fetchRequestedMyEntity.isPresent()) {
            MyEntity updatedMyEntity = fetchRequestedMyEntity.get();
            updatedMyEntity.setFirstName(myEntity.getFirstName());
            updatedMyEntity.setLastName(myEntity.getLastName());
            return ResponseEntity.ok(myEntityRepository.save(updatedMyEntity));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/my-entity")
    public ResponseEntity<List<MyEntity>> findAll() {
        return ResponseEntity.ok(myEntityRepository.findAll());
    }
}
