package org.example.server.ClientBankA.dto;

public class BankTransactionResponse {

    private String trxId;
    private String status;
    private String message;

    public BankTransactionResponse() {
    }

    public BankTransactionResponse(String trxId, String status, String message) {
        this.trxId = trxId;
        this.status = status;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
