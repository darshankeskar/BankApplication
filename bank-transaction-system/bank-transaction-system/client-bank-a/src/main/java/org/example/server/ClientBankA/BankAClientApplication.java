package org.example.server.ClientBankA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for Bank A client application.
 *
 * Responsibilities:
 * - Bootstraps the Spring Boot app that exposes /bank/transaction.
 * - Configuration and beans are defined in ClientConfig and other packages.
 */
@SpringBootApplication
public class BankAClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankAClientApplication.class, args);
    }
}
