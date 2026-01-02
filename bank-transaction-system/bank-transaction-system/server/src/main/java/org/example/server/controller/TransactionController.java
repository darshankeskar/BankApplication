package org.example.server.controller;


import org.example.server.dto.TransactionResponseDto;
import org.example.server.service.TransactionOrchestratorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * REST controller for the central server.
 *
 * Exposes:
 *   POST /server/transaction/process
 * Consumes:
 *   application/xml (TransactionRequest XML)
 * Produces:
 *   application/json (TransactionResponseDto)
 *
 * The controller is intentionally thin and delegates heavy work to
 * TransactionOrchestratorService.
 */
@RestController
@RequestMapping("/server/transaction")
public class TransactionController {

    private final TransactionOrchestratorService orchestratorService;

    public TransactionController(TransactionOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    /**
     * Accepts XML, forwards asynchronously to the processing executor,
     * and returns a CompletableFuture of the JSON response.
     */
    @PostMapping(
            value = "/process",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CompletableFuture<TransactionResponseDto> process(@RequestBody String xml) {
        return orchestratorService.processAsync(xml);
    }
}
