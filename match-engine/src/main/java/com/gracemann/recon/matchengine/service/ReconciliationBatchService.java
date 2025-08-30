package com.gracemann.recon.matchengine.service;

import com.gracemann.recon.matchengine.domain.BankSwitchTransactionLedger;
import com.gracemann.recon.matchengine.domain.ReconciliationBatchControl;
import com.gracemann.recon.matchengine.domain.SchemeSettlementTransactionLedger;
import com.gracemann.recon.matchengine.dto.BatchWindowConfig;
import com.gracemann.recon.matchengine.repository.ReconciliationBatchControlRepository;
import com.gracemann.recon.matchengine.util.BatchWindowConfigParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing the lifecycle and orchestration of reconciliation
 * batches.
 */
@Service
public class ReconciliationBatchService {

    private final ReconciliationBatchControlRepository batchRepo;
    private final LedgerFetchService ledgerFetchService;
    private final BatchWindowConfigParser configParser;

    public ReconciliationBatchService(
            ReconciliationBatchControlRepository batchRepo,
            LedgerFetchService ledgerFetchService,
            BatchWindowConfigParser configParser) {
        this.batchRepo = batchRepo;
        this.ledgerFetchService = ledgerFetchService;
        this.configParser = configParser;
    }

    /**
     * Creates and persists a new reconciliation batch, marking status as
     * PROCESSING.
     */
    @Transactional
    public ReconciliationBatchControl createNewBatch(String createdBy, String configSnapshot) {
        ReconciliationBatchControl batch = new ReconciliationBatchControl();
        batch.setBatchStartTimestamp(Timestamp.from(Instant.now()));
        batch.setBatchExecutionStatus("PROCESSING");
        batch.setBatchCreatedBy(createdBy);
        batch.setBatchConfigurationSnapshot(
                (configSnapshot == null || configSnapshot.isBlank()) ? "{}" : configSnapshot);

        return batchRepo.save(batch);
    }

    /**
     * MAIN ORCHESTRATION METHOD: Execute full batch processing.
     *
     * This method:
     * 1. Parses the window configuration from the batch
     * 2. Fetches ledger data for the window
     * 3. Runs matching logic (placeholder for now)
     * 4. Updates batch status and counters
     */
    @Transactional
    public void executeBatch(UUID batchId) {
        Optional<ReconciliationBatchControl> batchOpt = batchRepo.findById(batchId);
        if (batchOpt.isEmpty()) {
            throw new IllegalArgumentException("Batch not found: " + batchId);
        }

        ReconciliationBatchControl batch = batchOpt.get();

        try {
            // Step 1: Parse window configuration
            BatchWindowConfig windowConfig = parseWindowConfig(batch.getBatchConfigurationSnapshot());

            // Step 2: Fetch ledger data
            LedgerBatchWindow windowData = fetchLedgerTransactionsForWindow(
                    windowConfig.getWindowStart(),
                    windowConfig.getWindowEnd());

            // Step 3: Run matching logic (placeholder)
            BatchCounters results = runMatching(windowData);

            // Step 4: Complete the batch
            completeBatch(batchId, Timestamp.from(Instant.now()), results);

        } catch (Exception e) {
            // Mark batch as failed
            failBatch(batchId, Timestamp.from(Instant.now()), e.getMessage());
            throw new RuntimeException("Batch execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse window configuration from batch config snapshot.
     */
    private BatchWindowConfig parseWindowConfig(String configSnapshot) {
        try {
            return configParser.parseWindow(configSnapshot);
        } catch (Exception e) {
            // Fall back to default window if parsing fails
            return configParser.createDefaultWindow();
        }
    }

    /**
     * Fetches all ledger transactions within the specified window.
     */
    @Transactional(readOnly = true)
    public LedgerBatchWindow fetchLedgerTransactionsForWindow(LocalDateTime windowStart, LocalDateTime windowEnd) {
        List<BankSwitchTransactionLedger> bankTxns = ledgerFetchService.fetchBankTransactions(windowStart, windowEnd);
        List<SchemeSettlementTransactionLedger> schemeTxns = ledgerFetchService.fetchSchemeTransactions(windowStart,
                windowEnd);
        return new LedgerBatchWindow(bankTxns, schemeTxns);
    }

    /**
     * PLACEHOLDER: Run matching logic on the fetched data.
     * Replace this with your actual reconciliation engine.
     */
    private BatchCounters runMatching(LedgerBatchWindow windowData) {
        // TODO: Replace with actual matching logic
        int bankCount = windowData.bankTxns().size();
        int schemeCount = windowData.schemeTxns().size();
        int totalProcessed = bankCount + schemeCount;

        // Placeholder counters
        return new BatchCounters(
                totalProcessed,
                0, // exactMatches
                0, // fuzzyMatches
                bankCount, // unmatchedBank (all unmatched for now)
                schemeCount, // unmatchedScheme (all unmatched for now)
                0 // exceptions
        );
    }

    /**
     * Example method to test ledger fetching logic.
     */
    @Transactional(readOnly = true)
    public void testLedgerFetch() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        LedgerBatchWindow windowData = fetchLedgerTransactionsForWindow(start, end);

        System.out.println("Bank txns found: " + windowData.bankTxns().size());
        System.out.println("Scheme txns found: " + windowData.schemeTxns().size());
    }

    /**
     * Marks a batch as completed, updates all relevant counters and timestamps.
     */
    @Transactional
    public Optional<ReconciliationBatchControl> completeBatch(UUID batchId, Timestamp endTime, BatchCounters counters) {
        Optional<ReconciliationBatchControl> opt = batchRepo.findById(batchId);
        opt.ifPresent(batch -> {
            batch.setBatchEndTimestamp(endTime);
            batch.setBatchExecutionStatus("COMPLETED");
            batch.setTotalTransactionsProcessed(counters.totalProcessed());
            batch.setExactMatchesFound(counters.exactMatches());
            batch.setFuzzyMatchesFound(counters.fuzzyMatches());
            batch.setUnmatchedBankTransactions(counters.unmatchedBank());
            batch.setUnmatchedSchemeTransactions(counters.unmatchedScheme());
            batch.setExceptionCasesGenerated(counters.exceptions());
            batchRepo.save(batch);
        });
        return opt;
    }

    /**
     * Marks a batch as failed with end timestamp and optional error message.
     */
    @Transactional
    public Optional<ReconciliationBatchControl> failBatch(UUID batchId, Timestamp endTime, String error) {
        Optional<ReconciliationBatchControl> opt = batchRepo.findById(batchId);
        opt.ifPresent(batch -> {
            batch.setBatchEndTimestamp(endTime);
            batch.setBatchExecutionStatus("FAILED");
            batchRepo.save(batch);
        });
        return opt;
    }

    /**
     * Fetches the latest batch by start timestamp.
     */
    @Transactional(readOnly = true)
    public ReconciliationBatchControl getLatestBatch() {
        return batchRepo.findFirstByOrderByBatchStartTimestampDesc();
    }

    /**
     * Fetches all batches by execution status.
     */
    @Transactional(readOnly = true)
    public List<ReconciliationBatchControl> getBatchesByStatus(String status) {
        return batchRepo.findByBatchExecutionStatusOrderByBatchStartTimestampDesc(status);
    }

    /**
     * Fetches a batch by its UUID.
     */
    @Transactional(readOnly = true)
    public Optional<ReconciliationBatchControl> getBatchById(UUID batchId) {
        return batchRepo.findById(batchId);
    }

    /**
     * DTO for returning both ledgers' transactions for a given batch window.
     */
    public record LedgerBatchWindow(
            List<BankSwitchTransactionLedger> bankTxns,
            List<SchemeSettlementTransactionLedger> schemeTxns) {
    }

    /**
     * DTO for passing batch result counters to the completeBatch method.
     */
    public record BatchCounters(
            int totalProcessed,
            int exactMatches,
            int fuzzyMatches,
            int unmatchedBank,
            int unmatchedScheme,
            int exceptions) {
    }
}