package com.gracemann.recon.matchengine.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a normalized transaction record from the bank switch
 * ledger.
 * <p>
 * This entity is used to persist transaction data ingested from the bank
 * switch,
 * providing a canonical form for downstream reconciliation and matching
 * processes.
 * </p>
 *
 * <p>
 * <b>Table:</b> bank_switch_transaction_ledger
 * </p>
 *
 * <p>
 * <b>Fields:</b>
 * <ul>
 * <li><b>txnId</b>: Unique transaction identifier from the bank switch (Primary
 * Key).</li>
 * <li><b>cardNumber</b>: Masked primary account number (PAN).</li>
 * <li><b>amount</b>: Transaction amount (decimal, 2 places).</li>
 * <li><b>txnTimestamp</b>: Date and time of the transaction as recorded by the
 * switch.</li>
 * <li><b>currency</b>: ISO 4217 currency code (e.g., 'INR').</li>
 * <li><b>merchantId</b>: Optional merchant/acquirer identifier.</li>
 * <li><b>terminalId</b>: Optional terminal or device identifier.</li>
 * <li><b>responseCode</b>: Switch/ISO 8583 response code.</li>
 * <li><b>channel</b>: Channel type (e.g., POS, ATM, ECOM).</li>
 * <li><b>authCode</b>: Optional authorization code.</li>
 * <li><b>recordCreatedTimestamp</b>: Timestamp when the record was
 * created.</li>
 * <li><b>rawSourceRecord</b>: Raw source record for trace/debug purposes.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage:</b> This entity is managed by JPA/Hibernate and should not contain
 * business logic.
 * </p>
 *
 * @author [Your Name]
 * @since 1.0
 */
@Entity
@Table(name = "bank_switch_transaction_ledger")
public class BankSwitchTransactionLedger {

    @Id
    @Column(name = "transaction_id", nullable = false, unique = true, length = 50)
    private String txnId; // Transaction ID from bank switch (should be unique)

    @Column(name = "transaction_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount; // Transaction amount

    @Column(name = "transaction_timestamp", nullable = false)
    private LocalDateTime txnTimestamp; // Switch transaction timestamp

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currency; // ISO 4217 currency code

    @Column(name = "merchant_identifier", length = 50)
    private String merchantId; // Optional: Merchant/acquirer ID

    @Column(name = "masked_card_number", nullable = false, length = 20)
    private String cardNumber; // Masked PAN

    @Column(name = "terminal_identifier", length = 50)
    private String terminalId; // Optional: Terminal/device ID

    @Column(name = "switch_response_code", length = 10)
    private String responseCode; // Switch/ISO 8583 response code

    @Column(name = "transaction_channel", length = 20)
    private String channel; // POS/ATM/ECOM, etc.

    @Column(name = "authorization_code", length = 20)
    private String authCode; // Optional: Authorization code

    @Column(name = "record_created_timestamp")
    private LocalDateTime recordCreatedTimestamp; // Record creation timestamp

    @Column(name = "raw_source_message", columnDefinition = "text")
    private String rawSourceRecord; // For trace/debug

    // Add constructors, getters, setters

    public BankSwitchTransactionLedger() {
    }

    public BankSwitchTransactionLedger(
            String txnId,
            String cardNumber,
            BigDecimal amount,
            LocalDateTime txnTimestamp,
            String currency,
            String merchantId,
            String terminalId,
            String responseCode,
            String channel,
            String authCode,
            String rawSourceRecord) {
        this.txnId = txnId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.txnTimestamp = txnTimestamp;
        this.currency = currency;
        this.merchantId = merchantId;
        this.terminalId = terminalId;
        this.responseCode = responseCode;
        this.channel = channel;
        this.authCode = authCode;
        this.rawSourceRecord = rawSourceRecord;
    }

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

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public LocalDateTime getRecordCreatedTimestamp() {
        return recordCreatedTimestamp;
    }

    public void setRecordCreatedTimestamp(LocalDateTime recordCreatedTimestamp) {
        this.recordCreatedTimestamp = recordCreatedTimestamp;
    }

    public String getRawSourceRecord() {
        return rawSourceRecord;
    }

    public void setRawSourceRecord(String rawSourceRecord) {
        this.rawSourceRecord = rawSourceRecord;
    }

    // Standard getters and setters below (generate via IDE or Lombok if configured)
    // ... (for brevity, not pasting, but use your IDE's generate feature)

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BankSwitchTransactionLedger that = (BankSwitchTransactionLedger) o;
        return txnId != null && txnId.equals(that.txnId);
    }

    @Override
    public int hashCode() {
        return txnId != null ? txnId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "BankSwitchTransactionLedger{" +
                "txnId='" + txnId + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", amount=" + amount +
                ", txnTimestamp=" + txnTimestamp +
                ", currency='" + currency + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", channel='" + channel + '\'' +
                ", authCode='" + authCode + '\'' +
                ", recordCreatedTimestamp=" + recordCreatedTimestamp +
                ", rawSourceRecord='" + rawSourceRecord + '\'' +
                '}';
    }

}
