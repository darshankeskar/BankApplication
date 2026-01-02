package org.example.server.ClientBankA.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Asynchronous client for sending XML requests from Bank A to the central server.
 **/
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

                // We don't care about the response here, this is fire-and-forget.
                restTemplate.exchange(request, String.class);
            } catch (Exception e) {
                // In a real app: log the error with a logger
            }
        }, executor);
    }
}
