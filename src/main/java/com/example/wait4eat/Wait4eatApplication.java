package com.example.wait4eat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
@EntityScan(basePackages = "com.example.wait4eat.domain")
public class Wait4eatApplication {

    public static void main(String[] args) {
        SpringApplication.run(Wait4eatApplication.class, args);
    }

}
