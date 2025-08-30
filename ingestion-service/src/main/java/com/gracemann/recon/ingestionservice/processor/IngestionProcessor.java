package com.gracemann.recon.ingestionservice.processor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gracemann.recon.ingestionservice.dto.TxnRecordDTO;
import com.gracemann.recon.ingestionservice.metrics.IngestionMetrics;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Processes validated & deduplicated transactions and forwards them
 * to the match-engine topic.
 */
@Component
public class IngestionProcessor {

    private static final Logger log = LoggerFactory.getLogger(IngestionProcessor.class);
    private static final String MATCH_ENGINE_TOPIC = "ingested-txn-topic";

    /** Dedup by composite key = txnId|sourceType */
    private final Set<String> dedupSet = ConcurrentHashMap.newKeySet();

    private final Validator validator;
    private final IngestionMetrics metrics;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public IngestionProcessor(IngestionMetrics metrics,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.metrics = metrics;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /**
     * Validate → deduplicate → forward.
     */
    public void process(TxnRecordDTO dto) {
        metrics.incrementTotal();

        // 1️⃣ Validation -----------------------------------------------------
        Set<ConstraintViolation<TxnRecordDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            metrics.incrementErrors();
            logValidationErrors(dto, violations);
            return;
        }

        // 2️⃣ Deduplication (txnId + sourceType) ---------------------------
        String dedupKey = dto.getTxnId() + "|" + dto.getSourceType();
        if (!dedupSet.add(dedupKey)) {
            metrics.incrementDeduped();
            log.debug("Deduplicated: {}", dedupKey);
            return;
        }

        // 3️⃣ Forward to downstream topic -----------------------------------
        try {
            String json = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send(MATCH_ENGINE_TOPIC, dto.getTxnId(), json);
            metrics.incrementSuccess();
            log.info("Forwarded {} to downstream", dedupKey);
        } catch (Exception e) {
            metrics.incrementErrors();
            log.error("Forwarding failed for {}: {}", dedupKey, e.getMessage(), e);
            // TODO: retry / DLQ for prod
        }
    }

    /* --------------------------------------------------------------------- */

    private void logValidationErrors(TxnRecordDTO dto,
            Set<ConstraintViolation<TxnRecordDTO>> violations) {
        log.warn("Validation failed for txnId {}:", dto.getTxnId());
        violations.forEach(v -> log.warn("  {} {}", v.getPropertyPath(), v.getMessage()));
    }
}
//todo : all of this could another seperate class while into production