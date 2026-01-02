package org.example.server.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * JPA entity representing a persisted transaction log.
 *
 * Mapped to table 'transaction_log' with a unique constraint on trx_id.
 * Each row corresponds to one processed request (SUCCESS or FAILED).
 */
@Entity
@Table(name = "transaction_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_trx_id", columnNames = "trx_id")
})
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trx_id", nullable = false, unique = true)
    private String trxId;

    @Column(name = "bank_id", nullable = false)
    private String bankId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "from_account", nullable = false)
    private String fromAccount;

    @Column(name = "to_account", nullable = false)
    private String toAccount;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "reason")
    private String reason;

    @Column(name = "request_timestamp", nullable = false)
    private OffsetDateTime requestTimestamp;

    @Column(name = "processed_timestamp", nullable = false)
    private OffsetDateTime processedTimestamp;

    @Column(name = "processing_time_ms", nullable = false)
    private Long processingTimeMs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrxId() {
        return trxId;
    }

    public void setTrxId(String trxId) {
        this.trxId = trxId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public OffsetDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(OffsetDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public OffsetDateTime getProcessedTimestamp() {
        return processedTimestamp;
    }

    public void setProcessedTimestamp(OffsetDateTime processedTimestamp) {
        this.processedTimestamp = processedTimestamp;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

}
