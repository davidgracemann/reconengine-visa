package com.gracemann.recon.ingestionservice.producer;

/**
 *
 * ----------------------------------------------------------------
 * EXTERNAL VISA/RUPAY SCHMES CLEARING FILES
 * ----------------------------------------------------------------
 * Raw CSV test message constants for scheme transaction ingestion testing.
 *
 * <p>
 * This class provides static constants containing raw CSV line strings that
 * simulate
 * scheme transaction data files. These messages intentionally include some
 * "dirty" data
 * with missing fields, empty values, and malformed entries to test the
 * ingestion pipeline's
 * error handling and data validation capabilities.
 * </p>
 *
 * <p>
 * CSV format follows the order: date, schemeName, txnId, cardNumber, amount,
 * currency,
 * merchantId, terminalId, responseCode, batchId
 * </p>
 *
 * <p>
 * These test constants can be modified as needed to support various ingestion
 * test scenarios,
 * including both clean data and edge cases with data quality issues.
 * </p>
 *
 * @author Generated for ingestion testing
 * @version 1.0
 */
public class RawCsvTestMessages {

    /**
     * Clean scheme transaction - matches BANK-TXN-006 from ISO messages
     */
    public static final String VISA_CLEAN_TRANSACTION = "2024-07-15,VISA,BANK-TXN-667,4929001234567890,1750.50,INR,MERCH-201,TERM-201,00,BATCH-101";

    /**
     * Mastercard transaction with missing merchant ID - matches BANK-TXN-007
     */
    public static final String MASTERCARD_MISSING_MERCHANT = "2024-07-15,MASTERCARD,BANK-TXN-778,5412345678901234,8500.75,USD,,TERM-202,00,BATCH-102";

    /**
     * RuPay transaction with invalid currency code - matches BANK-TXN-008
     */
    public static final String RUPAY_INVALID_CURRENCY = "2024-07-15,RUPAY,BANK-TXN-889,6060123456789012,3000.25,XYZ,MERCH-203,TERM-203,05,BATCH-103";

    /**
     * AMEX transaction with malformed card number - matches BANK-TXN-009
     */
    public static final String AMEX_MALFORMED_CARD = "2024-07-15,AMEX,BANK-TXN-990,37412345678901,4200.00,USD,MERCH-204,TERM-204,00,BATCH-104";

    /**
     * Visa transaction with declined response code - matches BANK-TXN-010
     */
    public static final String VISA_DECLINED_TXN = "2024-07-15,VISA,BANK-TXN-101,4716123456789012,15000.00,INR,MERCH-205,TERM-205,14,BATCH-105";

    /**
     * CSV with extra commas and spaces (malformed structure) - additional test case
     */
    public static final String MALFORMED_EXTRA_COMMAS = "2024-07-15,MASTERCARD,BANK-TXN-202,5555444433332222, 750.00 ,INR,MERCH-206,,00,BATCH-106,EXTRA-FIELD";

    /**
     * Transaction with zero amount (edge case) - additional test case
     */
    public static final String ZERO_AMOUNT_TXN = "2024-07-15,RUPAY,BANK-TXN-303,6011234567890123,0.00,INR,MERCH-207,TERM-207,00,BATCH-107";

    /**
     * Transaction with non-numeric amount containing letters - additional test case
     */
    public static final String ALPHA_AMOUNT = "2024-07-15,VISA,BANK-TXN-404,4532876543210987,ABC.50,INR,MERCH-208,TERM-208,00,BATCH-108";

    /**
     * Future dated transaction (edge case) - additional test case
     */
    public static final String FUTURE_DATE_TXN = "2025-12-31,MASTERCARD,BANK-TXN-505,5234876543210987,2250.80,EUR,MERCH-209,TERM-209,00,BATCH-109";

    /**
     * Empty CSV line for testing empty record handling
     */
    public static final String EMPTY_LINE = "";

    /**
     * CSV header line (should be skipped during processing)
     */
    public static final String CSV_HEADER = "date,schemeName,txnId,cardNumber,amount,currency,merchantId,terminalId,responseCode,batchId";

    private RawCsvTestMessages() {
        // Utility class - prevent instantiation
    }

    public static final String[] ALL_MESSAGES = {
            VISA_CLEAN_TRANSACTION,
            MASTERCARD_MISSING_MERCHANT,
            RUPAY_INVALID_CURRENCY,
            AMEX_MALFORMED_CARD,
            VISA_DECLINED_TXN,
            MALFORMED_EXTRA_COMMAS,
            ZERO_AMOUNT_TXN,
            ALPHA_AMOUNT,
            FUTURE_DATE_TXN,
            EMPTY_LINE,
            CSV_HEADER
    };

}