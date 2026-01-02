package org.example.server.ClientBankA.controller;

import org.example.server.ClientBankA.dto.BankTransactionRequest;
import org.example.server.ClientBankA.dto.BankTransactionResponse;
import org.example.server.ClientBankA.service.ServerForwarder;
import org.example.server.ClientBankA.service.XmlConverter;
import org.example.server.ClientBankA.util.TransactionIdGenerator;
import org.example.server.ClientBankA.xml.TransactionRequestXml;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * REST controller for Bank A.
 *
 * Endpoint:
 *   POST /bank/transaction
 *
 * Responsibilities:
 * - Accept JSON transaction request.
 * - Generate unique trxId.
 * - Map JSON DTO to XML model and convert to XML string.
 * - Forward XML asynchronously to the central server.
 * - Immediately respond with status "FORWARDED".
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

        String trxId = idGenerator.nextId();

        TransactionRequestXml xmlRequest = new TransactionRequestXml();
        xmlRequest.setTrxId(trxId);
        xmlRequest.setBankId("BANK_A");
        xmlRequest.setCustomerId(request.getCustomerId());
        xmlRequest.setFromAccount(request.getFromAccount());
        xmlRequest.setToAccount(request.getToAccount());
        xmlRequest.setAmount(request.getAmount());
        xmlRequest.setCurrency(request.getCurrency());
        xmlRequest.setTimestamp(OffsetDateTime.now());

        String xml = xmlConverter.toXml(xmlRequest);

        // Asynchronous forwarding, non-blocking for this HTTP thread
        serverForwarder.forwardAsync(xml);

        BankTransactionResponse response =
                new BankTransactionResponse(trxId, "FORWARDED",
                        "Transaction forwarded to server");

        return ResponseEntity.accepted().body(response);
    }
}
