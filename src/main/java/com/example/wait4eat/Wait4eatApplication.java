package com.example.wait4eat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Wait4eatApplication {

    public static void main(String[] args) {
        SpringApplication.run(Wait4eatApplication.class, args);
    }

}
