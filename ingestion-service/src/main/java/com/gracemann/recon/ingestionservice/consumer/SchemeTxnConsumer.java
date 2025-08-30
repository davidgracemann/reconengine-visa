
/**
 * Consumer component responsible for consuming raw scheme transaction JSON
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

/**
 * Consumer component responsible for consuming raw scheme transaction JSON
 * strings from Kafka, parsing them into TxnRecordDTO objects,
 * and delegating processing to the IngestionProcessor.
 *
 * Uses stringKafkaListenerContainerFactory to consume raw JSON strings.
 */
@Component
public class SchemeTxnConsumer {

    private final ObjectMapper objectMapper;
    private final IngestionProcessor processor;

    // Constructor injection for both dependencies
    public SchemeTxnConsumer(ObjectMapper objectMapper, IngestionProcessor processor) {
        this.objectMapper = objectMapper;
        this.processor = processor;
    }

    @KafkaListener(topics = "scheme-txn-topic", groupId = "recon-ingestion-group", containerFactory = "stringKafkaListenerContainerFactory")
    public void consume(String rawJson) {
        try {
            TxnRecordDTO dto = objectMapper.readValue(rawJson, TxnRecordDTO.class);
            processor.process(dto);
        } catch (Exception e) {
            // TODO: Integrate with metrics and proper logging framework
            System.err.println("Error processing scheme txn: " + e.getMessage());
        }
    }
}
