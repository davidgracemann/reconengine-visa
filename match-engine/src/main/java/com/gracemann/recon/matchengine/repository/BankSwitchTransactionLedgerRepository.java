package com.gracemann.recon.matchengine.repository;

import com.gracemann.recon.matchengine.domain.BankSwitchTransactionLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BankSwitchTransactionLedger entities.
 */
@Repository
public interface BankSwitchTransactionLedgerRepository extends JpaRepository<BankSwitchTransactionLedger, String> {
    List<BankSwitchTransactionLedger> findByTxnTimestampBetween(LocalDateTime start, LocalDateTime end);
}