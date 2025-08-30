package com.gracemann.recon.matchengine.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MatchEngineTxnDTO {    

    private String txnId; // Unique transaction ID (switch or scheme)
    private String cardNumber; // Masked/tokenized PAN
    private BigDecimal amount; // Transaction amount
    private LocalDateTime txnTimestamp; // Transaction or settlement timestamp
    private String currency; // ISO 4217 code (e.g., 'INR')
    private String merchantId; // Acquirer/merchant identifier
    private String terminalId; // Terminal/device ID
    private String responseCode; // ISO 8583 or scheme response/status
    private String channel; // POS/ATM/ECOM (bank only)
    private String batchId; // Settlement batch/file ID (scheme only)
    private String schemeName; // Visa, RuPay, etc. (scheme only)
    private String authCode; // Authorization code (bank only)
    private SourceType sourceType; // BANK_SWITCH or SCHEME_FILE
    private String rawSourceRecord; // Original payload (ISO or scheme record), for trace/debug

    // Enum for distinguishing source
    public enum SourceType {
        BANK_SWITCH,
        SCHEME_FILE
    }

    // Constructors
    public MatchEngineTxnDTO() {
    }

    public MatchEngineTxnDTO(String txnId, String cardNumber, BigDecimal amount, LocalDateTime txnTimestamp,
            String currency, String merchantId, String terminalId, String responseCode,
            String channel, String batchId, String schemeName, String authCode,
            SourceType sourceType, String rawSourceRecord) {
        this.txnId = txnId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.txnTimestamp = txnTimestamp;
        this.currency = currency;
        this.merchantId = merchantId;
        this.terminalId = terminalId;
        this.responseCode = responseCode;
        this.channel = channel;
        this.batchId = batchId;
        this.schemeName = schemeName;
        this.authCode = authCode;
        this.sourceType = sourceType;
        this.rawSourceRecord = rawSourceRecord;
    }

    // Getters and Setters
    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTxnTimestamp() {
        return txnTimestamp;
    }

    public void setTxnTimestamp(LocalDateTime txnTimestamp) {
        this.txnTimestamp = txnTimestamp;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getRawSourceRecord() {
        return rawSourceRecord;
    }

    public void setRawSourceRecord(String rawSourceRecord) {
        this.rawSourceRecord = rawSourceRecord;
    }

    @Override
    public String toString() {
        return "MatchEngineTxnDTO{" +
                "txnId='" + txnId + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", amount=" + amount +
                ", txnTimestamp=" + txnTimestamp +
                ", currency='" + currency + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", channel='" + channel + '\'' +
                ", batchId='" + batchId + '\'' +
                ", schemeName='" + schemeName + '\'' +
                ", authCode='" + authCode + '\'' +
                ", sourceType=" + sourceType +
                ", rawSourceRecord='" + rawSourceRecord + '\'' +
                '}';
    }
}
