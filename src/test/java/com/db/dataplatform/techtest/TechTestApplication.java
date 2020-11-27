package com.db.dataplatform.techtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class TechTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechTestApplication.class, args);
    }
}