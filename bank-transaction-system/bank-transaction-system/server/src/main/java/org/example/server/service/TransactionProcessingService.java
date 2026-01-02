package org.example.server.service;


import org.example.server.dto.TransactionResponseDto;
import org.example.server.entity.TransactionLog;
import org.example.server.model.TransactionRequestXml;
import org.example.server.repository.TransactionLogRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core transactional service for processing a single TransactionRequestXml.
 *
 * This class:
 * - Validates request fields.
 * - Ensures trxId uniqueness using an in-memory ConcurrentHashMap for in-flight
 *   requests plus a UNIQUE constraint in the database.
 * - Applies simple business rules (insufficient balance, etc.).
 * - Persists a TransactionLog entry with processing timings.
 *
 * Concurrency:
 * - Annotated @Transactional so each call runs in a DB transaction.
 * - noRollbackFor = DataIntegrityViolationException ensures that duplicate trxId
 *   violations are treated as business failures instead of HTTP 500 errors.
 */

@Service
public class TransactionProcessingService {

    private final TransactionLogRepository logRepository;

    // Tracks in-flight transactions to avoid concurrent duplicates
    private final ConcurrentHashMap<String, Boolean> inFlightTransactions = new ConcurrentHashMap<>();

    public TransactionProcessingService(TransactionLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Process a single transaction request within a DB transaction.
     *
     * @param request   Parsed XML request.
     * @param startTime Time in ms when request handling started.
     * @return Structured response with status, reason, and processing time.
     */
    @Transactional(noRollbackFor = DataIntegrityViolationException.class)
    public TransactionResponseDto process(TransactionRequestXml request, long startTime) {
        String trxId = request.getTrxId();
        long processingTime;

        if (trxId != null) {
            Boolean existing = inFlightTransactions.putIfAbsent(trxId, Boolean.TRUE);
            if (existing != null) {
                processingTime = System.currentTimeMillis() - startTime;
                return new TransactionResponseDto(trxId, "FAILED",
                        "Duplicate Transaction (in-flight)", processingTime);
            }
        }

        try {
            String validationError = validate(request);
            if (validationError != null) {
                processingTime = System.currentTimeMillis() - startTime;
                saveLog(request, "FAILED", validationError, startTime, processingTime);
                return new TransactionResponseDto(trxId, "FAILED", validationError, processingTime);
            }

            String status = "SUCCESS";
            String reason = "Completed";

            if (request.getAmount().compareTo(new BigDecimal("100000")) > 0) {
                status = "FAILED";
                reason = "Insufficient Balance";
            }

            processingTime = System.currentTimeMillis() - startTime;

            try {
                saveLog(request, status, reason, startTime, processingTime);
            } catch (DataIntegrityViolationException ex) {
                status = "FAILED";
                reason = "Duplicate Transaction";
                processingTime = System.currentTimeMillis() - startTime;
            }

            return new TransactionResponseDto(trxId, status, reason, processingTime);
        } finally {
            if (trxId != null) {
                inFlightTransactions.remove(trxId);
            }
        }
    }

    /**
     * Performs basic field validation (ids, accounts, amount, currency).
     *
     * @return null if valid, or a human-readable error message.
     */
    private String validate(TransactionRequestXml r) {
        if (r == null) return "Null request";
        if (r.getTrxId() == null || r.getTrxId().isBlank()) return "Missing trxId";
        if (r.getBankId() == null || r.getBankId().isBlank()) return "Missing bankId";
        if (r.getFromAccount() == null || !r.getFromAccount().matches("\\d{10}"))
            return "Invalid fromAccount";
        if (r.getToAccount() == null || !r.getToAccount().matches("\\d{10}"))
            return "Invalid toAccount";
        if (r.getAmount() == null || r.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            return "Invalid amount";
        if (r.getCurrency() == null || r.getCurrency().isBlank())
            return "Invalid currency";
        return null;
    }

    /**
     * Persists the transaction log entry in the database.
     * This is the only place where we touch the TransactionLog entity.
     */
    private void saveLog(TransactionRequestXml r, String status, String reason,
                         long startTime, long processingTime) {
        TransactionLog log = new TransactionLog();
        log.setTrxId(r.getTrxId());
        log.setBankId(r.getBankId());
        log.setCustomerId(r.getCustomerId());
        log.setFromAccount(r.getFromAccount());
        log.setToAccount(r.getToAccount());
        log.setAmount(r.getAmount());
        log.setCurrency(r.getCurrency());
        log.setStatus(status);
        log.setReason(reason);
        log.setRequestTimestamp(r.getTimestamp() != null
                ? r.getTimestamp()
                : OffsetDateTime.now());
        log.setProcessedTimestamp(OffsetDateTime.now());
        log.setProcessingTimeMs(processingTime);

        logRepository.save(log);
    }
}
