package com.example.demo;

import com.example.demo.Entity.AnotherEntity;
import com.example.demo.Entity.MyEntity;
import com.example.demo.repository.AnotherEntityRepository;
import com.example.demo.repository.MyEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }

}
