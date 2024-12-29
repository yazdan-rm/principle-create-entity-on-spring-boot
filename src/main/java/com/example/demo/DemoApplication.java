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

    @Autowired
    private AnotherEntityRepository anotherEntityRepository;

    @Autowired
    private MyEntityRepository myEntityRepository;

    @Override
    public void run(String... args) throws Exception {
        MyEntity myEntity = new MyEntity();
        myEntity.setFirstName("John1");
        myEntity.setLastName("Doe1");
        var savedEntity = myEntityRepository.saveAndFlush(myEntity);

        savedEntity.setFirstName("test");
        var s1 = myEntityRepository.saveAndFlush(savedEntity);

        s1.setFirstName("test1");
        var s2 = myEntityRepository.saveAndFlush(s1);

        s2.setFirstName("test2");
        var s3 = myEntityRepository.saveAndFlush(s2);

        s3.setFirstName("test3");
        myEntityRepository.saveAndFlush(s3);

        AnotherEntity anotherEntity = new AnotherEntity();
        anotherEntity.setDescription("This is another entity1");
        anotherEntityRepository.saveAndFlush(anotherEntity);
    }

}
