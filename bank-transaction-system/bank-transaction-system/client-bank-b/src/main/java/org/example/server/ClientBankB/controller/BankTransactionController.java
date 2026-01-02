package org.example.server.ClientBankB.controller;


import org.example.server.ClientBankB.dto.BankTransactionRequest;
import org.example.server.ClientBankB.dto.BankTransactionResponse;
import org.example.server.ClientBankB.service.ServerForwarder;
import org.example.server.ClientBankB.service.XmlConverter;
import org.example.server.ClientBankB.util.TransactionIdGenerator;
import org.example.server.ClientBankB.xml.TransactionRequestXml;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

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
@RequestMapping("/bank")
public class BankTransactionController {

    private final TransactionIdGenerator idGenerator;
    private final XmlConverter xmlConverter;
    private final ServerForwarder serverForwarder;

    public BankTransactionController(TransactionIdGenerator idGenerator,
                                     XmlConverter xmlConverter,
                                     ServerForwarder serverForwarder) {
        this.idGenerator = idGenerator;
        this.xmlConverter = xmlConverter;
        this.serverForwarder = serverForwarder;
    }

    /**
     * Handles incoming JSON transaction request and forwards it to the server.
     */
    @PostMapping("/transaction")
    public ResponseEntity<BankTransactionResponse> handleTransaction(
            @RequestBody BankTransactionRequest request) {

        // 1. Generate transaction ID
        String trxId = idGenerator.nextId();

        // 2. Map JSON -> XML model
        TransactionRequestXml xmlRequest = new TransactionRequestXml();
        xmlRequest.setTrxId(trxId);
        xmlRequest.setBankId("BANK_B");
        xmlRequest.setCustomerId(request.getCustomerId());
        xmlRequest.setFromAccount(request.getFromAccount());
        xmlRequest.setToAccount(request.getToAccount());
        xmlRequest.setAmount(request.getAmount());
        xmlRequest.setCurrency(request.getCurrency());
        xmlRequest.setTimestamp(OffsetDateTime.now());

        // 3. Convert to XML string
        String xml = xmlConverter.toXml(xmlRequest);

        // 4. Async forward to central server
        serverForwarder.forwardAsync(xml);

        // 5. Immediate response
        BankTransactionResponse response =
                new BankTransactionResponse(trxId, "FORWARDED",
                        "Transaction forwarded to server");

        return ResponseEntity.accepted().body(response);
    }
}
