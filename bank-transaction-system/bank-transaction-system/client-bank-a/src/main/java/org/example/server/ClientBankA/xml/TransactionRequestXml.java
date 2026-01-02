package org.example.server.ClientBankA.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@JacksonXmlRootElement(localName = "TransactionRequest")
public class TransactionRequestXml {

    @JacksonXmlProperty(localName = "TrxId")
    private String trxId;

    @JacksonXmlProperty(localName = "BankId")
    private String bankId;

    @JacksonXmlProperty(localName = "CustomerId")
    private Long customerId;

    @JacksonXmlProperty(localName = "FromAccount")
    private String fromAccount;

    @JacksonXmlProperty(localName = "ToAccount")
    private String toAccount;

    @JacksonXmlProperty(localName = "Amount")
    private BigDecimal amount;

    @JacksonXmlProperty(localName = "Currency")
    private String currency;

    @JacksonXmlProperty(localName = "Timestamp")
    private OffsetDateTime timestamp;

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

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
