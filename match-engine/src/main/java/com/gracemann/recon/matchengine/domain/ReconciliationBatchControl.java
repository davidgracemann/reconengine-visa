package com.gracemann.recon.matchengine.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * JPA entity representing a reconciliation batch's control metadata.
 *
 * <p>
 * <b>Table:</b> reconciliation_batch_control
 * </p>
 *
 * <ul>
 * <li><b>batchExecutionId</b>: Unique UUID for each batch (Primary Key).</li>
 * <li><b>batchStartTimestamp</b>: When this batch started processing.</li>
 * <li><b>batchEndTimestamp</b>: When processing ended.</li>
 * <li><b>batchExecutionStatus</b>: Status (PENDING, IN_PROGRESS, COMPLETED,
 * FAILED).</li>
 * <li><b>totalTransactionsProcessed</b>: Total processed in this batch.</li>
 * <li><b>exactMatchesFound</b>, <b>fuzzyMatchesFound</b>,
 * <b>unmatchedBankTransactions</b>, <b>unmatchedSchemeTransactions</b>,
 * <b>exceptionCasesGenerated</b>: Result counters.</li>
 * <li><b>batchCreatedBy</b>: Operator/automation that created the batch.</li>
 * <li><b>batchConfigurationSnapshot</b>: JSON snapshot of rules/config at
 * execution time.</li>
 * <li><b>batchCreatedTimestamp</b>: Timestamp when this record was
 * inserted.</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b> This entity is managed by JPA/Hibernate and is used for
 * orchestration & audit.
 * </p>
 */
@Entity
@Table(name = "reconciliation_batch_control")
public class ReconciliationBatchControl {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "batch_execution_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID batchExecutionId;

    @Column(name = "batch_start_timestamp", nullable = false)
    private Timestamp batchStartTimestamp;

    @Column(name = "batch_end_timestamp")
    private Timestamp batchEndTimestamp;

    @Column(name = "batch_execution_status", nullable = false, length = 20)
    private String batchExecutionStatus;

    @Column(name = "total_transactions_processed", nullable = false)
    private int totalTransactionsProcessed = 0;

    @Column(name = "exact_matches_found", nullable = false)
    private int exactMatchesFound = 0;

    @Column(name = "fuzzy_matches_found", nullable = false)
    private int fuzzyMatchesFound = 0;

    @Column(name = "unmatched_bank_transactions", nullable = false)
    private int unmatchedBankTransactions = 0;

    @Column(name = "unmatched_scheme_transactions", nullable = false)
    private int unmatchedSchemeTransactions = 0;

    @Column(name = "exception_cases_generated", nullable = false)
    private int exceptionCasesGenerated = 0;

    @Column(name = "batch_created_by", nullable = false, length = 50)
    private String batchCreatedBy;

    @Column(name = "batch_configuration_snapshot", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String batchConfigurationSnapshot;

    @Column(name = "batch_created_timestamp", nullable = false, updatable = false, insertable = false)
    private Timestamp batchCreatedTimestamp;

    // --- Constructors ---
    public ReconciliationBatchControl() {
    }

    public ReconciliationBatchControl(
            Timestamp batchStartTimestamp,
            String batchExecutionStatus,
            String batchCreatedBy,
            String batchConfigurationSnapshot) {
        this.batchStartTimestamp = batchStartTimestamp;
        this.batchExecutionStatus = batchExecutionStatus;
        this.batchCreatedBy = batchCreatedBy;
        this.batchConfigurationSnapshot = batchConfigurationSnapshot;
    }

    // --- Getters and Setters ---

    public UUID getBatchExecutionId() {
        return batchExecutionId;
    }

    public void setBatchExecutionId(UUID batchExecutionId) {
        this.batchExecutionId = batchExecutionId;
    }

    public Timestamp getBatchStartTimestamp() {
        return batchStartTimestamp;
    }

    public void setBatchStartTimestamp(Timestamp batchStartTimestamp) {
        this.batchStartTimestamp = batchStartTimestamp;
    }

    public Timestamp getBatchEndTimestamp() {
        return batchEndTimestamp;
    }

    public void setBatchEndTimestamp(Timestamp batchEndTimestamp) {
        this.batchEndTimestamp = batchEndTimestamp;
    }

    public String getBatchExecutionStatus() {
        return batchExecutionStatus;
    }

    public void setBatchExecutionStatus(String batchExecutionStatus) {
        this.batchExecutionStatus = batchExecutionStatus;
    }

    public int getTotalTransactionsProcessed() {
        return totalTransactionsProcessed;
    }

    public void setTotalTransactionsProcessed(int totalTransactionsProcessed) {
        this.totalTransactionsProcessed = totalTransactionsProcessed;
    }

    public int getExactMatchesFound() {
        return exactMatchesFound;
    }

    public void setExactMatchesFound(int exactMatchesFound) {
        this.exactMatchesFound = exactMatchesFound;
    }

    public int getFuzzyMatchesFound() {
        return fuzzyMatchesFound;
    }

    public void setFuzzyMatchesFound(int fuzzyMatchesFound) {
        this.fuzzyMatchesFound = fuzzyMatchesFound;
    }

    public int getUnmatchedBankTransactions() {
        return unmatchedBankTransactions;
    }

    public void setUnmatchedBankTransactions(int unmatchedBankTransactions) {
        this.unmatchedBankTransactions = unmatchedBankTransactions;
    }

    public int getUnmatchedSchemeTransactions() {
        return unmatchedSchemeTransactions;
    }

    public void setUnmatchedSchemeTransactions(int unmatchedSchemeTransactions) {
        this.unmatchedSchemeTransactions = unmatchedSchemeTransactions;
    }

    public int getExceptionCasesGenerated() {
        return exceptionCasesGenerated;
    }

    public void setExceptionCasesGenerated(int exceptionCasesGenerated) {
        this.exceptionCasesGenerated = exceptionCasesGenerated;
    }

    public String getBatchCreatedBy() {
        return batchCreatedBy;
    }

    public void setBatchCreatedBy(String batchCreatedBy) {
        this.batchCreatedBy = batchCreatedBy;
    }

    public String getBatchConfigurationSnapshot() {
        return batchConfigurationSnapshot;
    }

    public void setBatchConfigurationSnapshot(String batchConfigurationSnapshot) {
        this.batchConfigurationSnapshot = batchConfigurationSnapshot;
    }

    public Timestamp getBatchCreatedTimestamp() {
        return batchCreatedTimestamp;
    }

    public void setBatchCreatedTimestamp(Timestamp batchCreatedTimestamp) {
        this.batchCreatedTimestamp = batchCreatedTimestamp;
    }

    // --- equals, hashCode, toString (optional, IDE generate or Lombok) ---

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ReconciliationBatchControl))
            return false;
        ReconciliationBatchControl that = (ReconciliationBatchControl) o;
        return batchExecutionId != null && batchExecutionId.equals(that.batchExecutionId);
    }

    @Override
    public int hashCode() {
        return batchExecutionId != null ? batchExecutionId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ReconciliationBatchControl{" +
                "batchExecutionId=" + batchExecutionId +
                ", batchStartTimestamp=" + batchStartTimestamp +
                ", batchEndTimestamp=" + batchEndTimestamp +
                ", batchExecutionStatus='" + batchExecutionStatus + '\'' +
                ", totalTransactionsProcessed=" + totalTransactionsProcessed +
                ", exactMatchesFound=" + exactMatchesFound +
                ", fuzzyMatchesFound=" + fuzzyMatchesFound +
                ", unmatchedBankTransactions=" + unmatchedBankTransactions +
                ", unmatchedSchemeTransactions=" + unmatchedSchemeTransactions +
                ", exceptionCasesGenerated=" + exceptionCasesGenerated +
                ", batchCreatedBy='" + batchCreatedBy + '\'' +
                ", batchConfigurationSnapshot='" + batchConfigurationSnapshot + '\'' +
                ", batchCreatedTimestamp=" + batchCreatedTimestamp +
                '}';
    }
}
