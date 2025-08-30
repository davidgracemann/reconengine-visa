package com.gracemann.recon.ingestionservice.metrics;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * IngestionMetrics tracks various counters for the ingestion pipeline,
 * including:
 * <ul>
 * <li>Total transactions received</li>
 * <li>Duplicate transactions filtered</li>
 * <li>Successfully processed transactions</li>
 * <li>Errored transactions</li>
 * </ul>
 *
 * <p>
 * This component provides thread-safe counters for monitoring ingestion health
 * and supports integration with metrics frameworks (e.g., Micrometer) in the
 * future.
 * </p>
 *
 * <p>
 * Usage: Inject and increment counters during processing to track pipeline
 * status.
 * </p>
 *
 * @author
 * @version 1.0
 */
@Component
public class IngestionMetrics {

    private final AtomicLong totalTransactions = new AtomicLong(0);
    private final AtomicLong dedupedTransactions = new AtomicLong(0);
    private final AtomicLong successfulTransactions = new AtomicLong(0);
    private final AtomicLong errorTransactions = new AtomicLong(0);

    /**
     * Increment the count of total transactions received.
     */
    public void incrementTotal() {
        totalTransactions.incrementAndGet();
    }

    /**
     * Increment the count of transactions identified as duplicates and filtered.
     */
    public void incrementDeduped() {
        dedupedTransactions.incrementAndGet();
    }

    /**
     * Increment the count of transactions successfully processed and forwarded.
     */
    public void incrementSuccess() {
        successfulTransactions.incrementAndGet();
    }

    /**
     * Increment the count of transactions that failed processing due to errors.
     */
    public void incrementErrors() {
        errorTransactions.incrementAndGet();
    }

    /**
     * Get the total number of transactions received.
     *
     * @return total transaction count
     */
    public long getTotalTransactions() {
        return totalTransactions.get();
    }

    /**
     * Get the number of transactions filtered as duplicates.
     *
     * @return deduplication count
     */
    public long getDedupedTransactions() {
        return dedupedTransactions.get();
    }

    /**
     * Get the number of transactions successfully processed.
     *
     * @return success count
     */
    public long getSuccessfulTransactions() {
        return successfulTransactions.get();
    }

    /**
     * Get the number of transactions that encountered errors during processing.
     *
     * @return error count
     */
    public long getErrorTransactions() {
        return errorTransactions.get();
    }

    @Override
    public String toString() {
        return "IngestionMetrics{" +
                "totalTransactions=" + getTotalTransactions() +
                ", dedupedTransactions=" + getDedupedTransactions() +
                ", successfulTransactions=" + getSuccessfulTransactions() +
                ", errorTransactions=" + getErrorTransactions() +
                '}';
    }
}
