package com.gracemann.recon.ingestionservice.producer;

/**
 * ----------------------------------------------------------------
 * INTERNAL BANK LEDGER RAW ISO 8583 MESSAGE PRODUCER SAMPLES
 * ----------------------------------------------------------------
 *
 * Raw ISO8583-like test message constants for bank transaction ingestion
 * testing.
 *
 * <p>
 * This class provides static constants containing pipe-delimited raw message
 * strings
 * that simulate bank transaction data in ISO8583-like format. These messages
 * are designed
 * to be pristine and well-formed for testing the ingestion pipeline's happy
 * path scenarios.
 * </p>
 *
 * <p>
 * Each message contains all required fields for bank transactions including:
 * MTI, txnId, cardNumber, amount, txnTimestamp, currency, merchantId,
 * terminalId,
 * responseCode, channel, and authCode.
 * </p>
 *
 * <p>
 * These test constants can be modified as needed to support various ingestion
 * test scenarios.
 * </p>
 *
 * @author Generated for ingestion testing
 * @version 1.0
 */
public class RawIsoTestMessages {

    /**
     * Sample POS transaction message - successful purchase
     */
    public static final String POS_PURCHASE_SUCCESS = "MTI=0200|txnId=BANK-TXN-667|cardNumber=4929001234567890|amount=1750.50|"
            + "txnTimestamp=2024-07-15T09:15:22|currency=INR|merchantId=MERCH-201|"
            + "terminalId=TERM-201|responseCode=00|channel=POS|authCode=AUTH456789";

    /**
     * Sample ATM withdrawal message - successful transaction
     */
    public static final String ATM_WITHDRAWAL_SUCCESS = "MTI=0200|txnId=BANK-TXN-778|cardNumber=5412345678901234|amount=8500.75|"
            + "txnTimestamp=2024-07-15T11:30:45|currency=USD|merchantId=MERCH-202|"
            + "terminalId=TERM-202|responseCode=00|channel=ATM|authCode=AUTH234567";

    /**
     * Sample online transaction message - successful e-commerce purchase
     */
    public static final String ONLINE_PURCHASE_SUCCESS = "MTI=0200|txnId=BANK-TXN-889|cardNumber=6060123456789012|amount=3000.25|"
            + "txnTimestamp=2024-07-15T13:45:18|currency=INR|merchantId=MERCH-203|"
            + "terminalId=TERM-203|responseCode=00|channel=ONLINE|authCode=AUTH678901";

    /**
     * Sample mobile banking transaction message - successful transfer
     */
    public static final String MOBILE_TRANSFER_SUCCESS = "MTI=0200|txnId=BANK-TXN-990|cardNumber=3741234567890123|amount=4200.00|"
            + "txnTimestamp=2024-07-15T16:20:33|currency=USD|merchantId=MERCH-204|"
            + "terminalId=TERM-204|responseCode=00|channel=MOBILE|authCode=AUTH890123";

    /**
     * Sample declined transaction message - insufficient funds
     */
    public static final String POS_PURCHASE_DECLINED = "MTI=0210|txnId=BANK-TXN-101|cardNumber=4716123456789012|amount=15000.00|"
            + "txnTimestamp=2024-07-15T18:10:55|currency=INR|merchantId=MERCH-205|"
            + "terminalId=TERM-205|responseCode=51|channel=POS|authCode=";

    private RawIsoTestMessages() {
        // Utility class - prevent instantiation
    }

    public static final String[] ALL_MESSAGES = {
            POS_PURCHASE_SUCCESS,
            ATM_WITHDRAWAL_SUCCESS,
            ONLINE_PURCHASE_SUCCESS,
            MOBILE_TRANSFER_SUCCESS,
            POS_PURCHASE_DECLINED
    };

}