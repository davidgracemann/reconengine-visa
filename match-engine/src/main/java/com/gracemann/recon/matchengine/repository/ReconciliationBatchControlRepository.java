package com.gracemann.recon.matchengine.repository;

import com.gracemann.recon.matchengine.domain.ReconciliationBatchControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ReconciliationBatchControl entities.
 */
@Repository
public interface ReconciliationBatchControlRepository extends JpaRepository<ReconciliationBatchControl, UUID> {
    // Find all batches by status, newest first
    List<ReconciliationBatchControl> findByBatchExecutionStatusOrderByBatchStartTimestampDesc(String status);

    // Find latest batch
    ReconciliationBatchControl findFirstByOrderByBatchStartTimestampDesc();
}
