package com.gracemann.recon.matchengine.repository;

import com.gracemann.recon.matchengine.domain.SchemeSettlementTransactionLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SchemeSettlementTransactionLedger entities.
 */
@Repository
public interface SchemeSettlementTransactionLedgerRepository
        extends JpaRepository<SchemeSettlementTransactionLedger, String> {
    List<SchemeSettlementTransactionLedger> findByTxnTimestampBetween(LocalDateTime start, LocalDateTime end);
}