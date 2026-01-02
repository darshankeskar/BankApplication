package org.example.server.dto;

public class TransactionResponseDto {

    private String trxId;
    private String status;
    private String reason;
    private long processingTimeMs;

    public TransactionResponseDto() {}

    public TransactionResponseDto(String trxId, String status, String reason, long processingTimeMs) {
        this.trxId = trxId;
        this.status = status;
        this.reason = reason;
        this.processingTimeMs = processingTimeMs;
    }

    public String getTrxId() {
        return trxId;
    }

    public void setTrxId(String trxId) {
        this.trxId = trxId;
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

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

}