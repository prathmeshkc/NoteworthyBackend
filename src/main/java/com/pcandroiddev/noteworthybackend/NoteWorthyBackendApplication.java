package com.pcandroiddev.noteworthybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NoteWorthyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteWorthyBackendApplication.class, args);
    }

}
