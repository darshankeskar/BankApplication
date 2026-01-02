package org.example.server.ClientBankB.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Asynchronously forwards Bank B transactions to the central server.
 */
@Component
public class ServerForwarder {

    private final ExecutorService executor;
    private final RestTemplate restTemplate;
    private final String serverUrl;

    public ServerForwarder(ExecutorService executor,
                           RestTemplate restTemplate,
                           @Value("${central-server.transaction-url}") String serverUrl) {
        this.executor = executor;
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public CompletableFuture<Void> forwardAsync(String xml) {
        return CompletableFuture.runAsync(() -> {
            try {
                RequestEntity<String> request = RequestEntity
                        .post(URI.create(serverUrl))
                        .contentType(MediaType.APPLICATION_XML)
                        .body(xml);

                restTemplate.exchange(request, String.class);
            } catch (Exception e) {
                // log error in real application
            }
        }, executor);
    }
}
