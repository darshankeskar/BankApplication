package org.example.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the central transaction server.
 *
 * Responsibilities:
 * - Bootstraps the Spring Boot application.
 * - Loads web, JPA, and datasource configuration from application.yml.
 * - Exposes the REST endpoint /server/transaction/process.
 *
 * All business logic is delegated to controller and service layers.
 */
@SpringBootApplication
public class BankServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankServerApplication.class, args);
    }
}