
/**
 * Consumer component responsible for consuming raw bank transaction JSON
 * strings
 * from Kafka, parsing them into TxnRecordDTO objects, and delegating processing
 * to the IngestionProcessor.
 *
 * Uses stringKafkaListenerContainerFactory to consume raw JSON strings.
 */
package com.gracemann.recon.ingestionservice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gracemann.recon.ingestionservice.dto.TxnRecordDTO;
import com.gracemann.recon.ingestionservice.processor.IngestionProcessor;

@Component
public class BankTxnConsumer {

    private final ObjectMapper objectMapper;
    private final IngestionProcessor processor;

    // Constructor injection for all dependencies
    public BankTxnConsumer(ObjectMapper objectMapper, IngestionProcessor processor) {
        this.objectMapper = objectMapper;
        this.processor = processor;
    }

    @KafkaListener(
        topics = "bank-txn-topic",
        groupId = "recon-ingestion-group",
        containerFactory = "stringKafkaListenerContainerFactory"
    )
    public void consume(String rawJson) {
        try {
            TxnRecordDTO dto = objectMapper.readValue(rawJson, TxnRecordDTO.class);
            processor.process(dto);
        } catch (Exception e) {
            // TODO: Use proper logging
            System.err.println("Error processing bank txn: " + e.getMessage());
        }
    }
}
