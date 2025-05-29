package com.mediconnect.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MediConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediConnectApplication.class, args);
    }
} 