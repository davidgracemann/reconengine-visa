package com.gracemann.recon.matchengine.service;

import com.gracemann.recon.matchengine.domain.BankSwitchTransactionLedger;
import com.gracemann.recon.matchengine.domain.SchemeSettlementTransactionLedger;
import com.gracemann.recon.matchengine.repository.BankSwitchTransactionLedgerRepository;
import com.gracemann.recon.matchengine.repository.SchemeSettlementTransactionLedgerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to fetch ledger transactions for reconciliation batches.
 * Encapsulates all query logic for data windows, filters, and batch
 * preparation.
 *
 * This service abstracts fetching for both the bank switch and scheme
 * settlement ledgers,
 * given a batch reconciliation window (start/end). Used by the batch
 * orchestration layer.
 *
 * @author YourName
 * @since 1.0
 */
@Service
public class LedgerFetchService {

    private final BankSwitchTransactionLedgerRepository bankRepo;
    private final SchemeSettlementTransactionLedgerRepository schemeRepo;

    /**
     * Constructor injection for required repositories.
     */
    public LedgerFetchService(
            BankSwitchTransactionLedgerRepository bankRepo,
            SchemeSettlementTransactionLedgerRepository schemeRepo) {
        this.bankRepo = bankRepo;
        this.schemeRepo = schemeRepo;
    }

    /**
     * Fetches all bank switch ledger transactions within the specified window
     * (inclusive).
     *
     * @param windowStart Start timestamp (inclusive)
     * @param windowEnd   End timestamp (inclusive)
     * @return List of matching BankSwitchTransactionLedger entities
     */
    public List<BankSwitchTransactionLedger> fetchBankTransactions(LocalDateTime windowStart, LocalDateTime windowEnd) {
        // You must add this method to your repository interface:
        // List<BankSwitchTransactionLedger> findByTxnTimestampBetween(LocalDateTime
        // start, LocalDateTime end);
        return bankRepo.findByTxnTimestampBetween(windowStart, windowEnd);
    }

    /**
     * Fetches all scheme settlement ledger transactions within the specified window
     * (inclusive).
     *
     * @param windowStart Start timestamp (inclusive)
     * @param windowEnd   End timestamp (inclusive)
     * @return List of matching SchemeSettlementTransactionLedger entities
     */
    public List<SchemeSettlementTransactionLedger> fetchSchemeTransactions(LocalDateTime windowStart,
            LocalDateTime windowEnd) {
        // You must add this method to your repository interface:
        // List<SchemeSettlementTransactionLedger>
        // findByTxnTimestampBetween(LocalDateTime start, LocalDateTime end);
        return schemeRepo.findByTxnTimestampBetween(windowStart, windowEnd);
    }
}
