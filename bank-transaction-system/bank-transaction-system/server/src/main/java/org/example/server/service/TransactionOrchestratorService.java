package org.example.server.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.server.dto.TransactionResponseDto;
import org.example.server.model.TransactionRequestXml;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Orchestrates XML unmarshalling and asynchronous processing.
 *
 * Responsibilities:
 * - Parse incoming XML into TransactionRequestXml via XmlMapper.
 * - Submit processing work to the transactionExecutor (ExecutorService).
 * - Wraps result in a CompletableFuture for non-blocking HTTP handling.
 *
 * This service separates transport concerns from core processing logic.
 */

@Service
public class TransactionOrchestratorService {

    private final XmlMapper xmlMapper;
    private final ExecutorService transactionExecutor;
    private final TransactionProcessingService processingService;

    public TransactionOrchestratorService(XmlMapper xmlMapper,
                                          ExecutorService transactionExecutor,
                                          TransactionProcessingService processingService) {
        this.xmlMapper = xmlMapper;
        this.transactionExecutor = transactionExecutor;
        this.processingService = processingService;
    }

    /**
     * Asynchronously unmarshals XML and processes the transaction.
     */
    public CompletableFuture<TransactionResponseDto> processAsync(String xml) {
        long startTime = System.currentTimeMillis();

        return CompletableFuture.supplyAsync(() -> {
            TransactionRequestXml request;
            try {
                request = xmlMapper.readValue(xml, TransactionRequestXml.class);
            } catch (Exception e) {
                long time = System.currentTimeMillis() - startTime;
                // Validation error: invalid XML format
                return new TransactionResponseDto(null, "FAILED",
                        "Invalid XML format", time);
            }

            // Delegates to transactional service for core business logic.
            return processingService.process(request, startTime);
        }, transactionExecutor);
    }
}
