package com.gracemann.recon.matchengine.kafka;

import com.gracemann.recon.matchengine.dto.MatchEngineTxnDTO;
import com.gracemann.recon.matchengine.domain.BankSwitchTransactionLedger;
import com.gracemann.recon.matchengine.domain.SchemeSettlementTransactionLedger;
import com.gracemann.recon.matchengine.repository.BankSwitchTransactionLedgerRepository;
import com.gracemann.recon.matchengine.repository.SchemeSettlementTransactionLedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer service for ingesting normalized transaction DTOs and
 * persisting them
 * to the appropriate ledger tables (bank switch or scheme settlement) based on
 * source type.
 * <p>
 * This service listens to the 'ingested-txn-topic' Kafka topic, receives
 * normalized transaction
 * records as {@link com.gracemann.recon.matchengine.dto.MatchEngineTxnDTO},
 * maps them to the
 * appropriate JPA entity, and persists them using the corresponding repository.
 * It supports both
 * bank switch and scheme settlement sources, and logs all operations for
 * traceability and audit.
 * <ul>
 * <li>Bank switch transactions are saved to
 * {@link BankSwitchTransactionLedgerRepository}.</li>
 * <li>Scheme settlement transactions are saved to
 * {@link SchemeSettlementTransactionLedgerRepository}.</li>
 * </ul>
 * <b>Error Handling:</b> Any exceptions during persistence are logged as
 * errors. Unknown or null source types are also logged.
 *
 * @author [Your Name]
 * @since 1.0
 */
@Service
public class IngestedNormalizedTxnKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(IngestedNormalizedTxnKafkaConsumer.class);

    private final BankSwitchTransactionLedgerRepository bankRepo;
    private final SchemeSettlementTransactionLedgerRepository schemeRepo;

    // Spring will inject both repositories via constructor injection
    public IngestedNormalizedTxnKafkaConsumer(
            BankSwitchTransactionLedgerRepository bankRepo,
            SchemeSettlementTransactionLedgerRepository schemeRepo) {
        this.bankRepo = bankRepo;
        this.schemeRepo = schemeRepo;
    }

    /**
     * Listens to the 'ingested-txn-topic' Kafka topic, receives normalized
     * transaction DTOs,
     * maps them to the correct entity, and saves them to the appropriate ledger
     * table.
     * <p>
     * Handles both bank switch and scheme settlement transactions. Logs all actions
     * and errors.
     * </p>
     *
     * @param dto the normalized transaction data transfer object received from the
     *            ingestion engine
     */
    @KafkaListener(topics = "ingested-txn-topic", groupId = "match-engine-consumer-group")
    public void consume(MatchEngineTxnDTO dto) {
        log.info("Received normalized ledger entry from ingestion engine: {}", dto);

        try {
            if (dto.getSourceType() == null) {
                log.warn("Received DTO with null SourceType: txnId={}", dto.getTxnId());
                return;
            }
            switch (dto.getSourceType()) {
                case BANK_SWITCH -> {
                    BankSwitchTransactionLedger bankEntity = new BankSwitchTransactionLedger(
                            dto.getTxnId(),
                            dto.getCardNumber(),
                            dto.getAmount(),
                            dto.getTxnTimestamp(),
                            dto.getCurrency(),
                            dto.getMerchantId(),
                            dto.getTerminalId(),
                            dto.getResponseCode(),
                            dto.getChannel(),
                            dto.getAuthCode(),
                            dto.getRawSourceRecord());
                    bankRepo.save(bankEntity);
                    log.info("Saved to bank_switch_transaction_ledger: txnId={}", dto.getTxnId());
                }
                case SCHEME_FILE -> {
                    SchemeSettlementTransactionLedger schemeEntity = new SchemeSettlementTransactionLedger(
                            dto.getTxnId(),
                            dto.getCardNumber(),
                            dto.getAmount(),
                            dto.getTxnTimestamp(),
                            dto.getCurrency(),
                            dto.getMerchantId(),
                            dto.getTerminalId(),
                            dto.getResponseCode(),
                            dto.getBatchId(),
                            dto.getSchemeName(),
                            dto.getRawSourceRecord());
                    schemeRepo.save(schemeEntity);
                    log.info("Saved to scheme_settlement_transaction_ledger: txnId={}", dto.getTxnId());
                }
                default -> log.warn("Unknown SourceType: {} for txnId={}", dto.getSourceType(), dto.getTxnId());
            }
        } catch (Exception e) {
            log.error("Failed to persist ledger entry: txnId={}, error={}", dto.getTxnId(), e.getMessage(), e);
            // Optional: push to DLQ or exception topic if desired
        }
    }
}
