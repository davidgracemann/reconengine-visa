package com.gracemann.recon.ingestionservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * KafkaConfig is the central configuration for all Kafka producer and consumer
 * beans
 * in the ingestion-service. Supports both string (raw JSON) and DTO-based
 * flows.
 *
 * Usage:
 * - Use {@code KafkaTemplate<String, String>} to send raw JSON payloads to
 * Kafka.
 * - Use
 * {@code @KafkaListener(..., containerFactory = "stringKafkaListenerContainerFactory")}
 * to consume raw JSON strings from Kafka.
 * - (Optional) Use DTO-based templates/container factories for advanced use
 * cases.
 *
 * This config is production-ready, easy to extend, and matches common Spring
 * Boot Kafka best practices.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    /**
     * ProducerFactory for sending raw JSON strings to Kafka.
     *
     * Use this to produce raw JSON as String (recommended for most ingestion E2E
     * flows).
     */
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Optional tuning (e.g., acks, retries) can be added here.
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * KafkaTemplate for sending raw JSON strings.
     *
     * Usage: Autowire this bean to send JSON strings to any topic.
     */
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    /**
     * ConsumerFactory for receiving raw JSON strings from Kafka.
     *
     * Use this to consume JSON as String, then manually map to DTO in your
     * listener.
     */
    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "recon-ingestion-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // Optional tuning (e.g., max poll records, auto offset reset) can be added
        // here.
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * KafkaListenerContainerFactory for consuming raw JSON strings.
     *
     * Usage: Specify this factory in your @KafkaListener if you want to receive
     * String payloads.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        return factory;
    }

    // --- (Optional) DTO-based Producer/Consumer Factories for advanced usage ---

    // Uncomment/extend if you want to support direct DTO <-> Kafka serialization in
    // the future.
    /*
     * @Bean
     * public ProducerFactory<String, TxnRecordDTO> dtoProducerFactory() {
     * Map<String, Object> props = new HashMap<>();
     * props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
     * props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
     * StringSerializer.class);
     * props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
     * JsonSerializer.class);
     * return new DefaultKafkaProducerFactory<>(props);
     * }
     *
     * @Bean
     * public KafkaTemplate<String, TxnRecordDTO> dtoKafkaTemplate() {
     * return new KafkaTemplate<>(dtoProducerFactory());
     * }
     *
     * @Bean
     * public ConsumerFactory<String, TxnRecordDTO> dtoConsumerFactory() {
     * Map<String, Object> props = new HashMap<>();
     * props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
     * props.put(ConsumerConfig.GROUP_ID_CONFIG, "recon-ingestion-group");
     * props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
     * StringDeserializer.class);
     * props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
     * JsonDeserializer.class);
     * props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
     * return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new
     * JsonDeserializer<>(TxnRecordDTO.class, false));
     * }
     *
     * @Bean
     * public ConcurrentKafkaListenerContainerFactory<String, TxnRecordDTO>
     * dtoKafkaListenerContainerFactory() {
     * ConcurrentKafkaListenerContainerFactory<String, TxnRecordDTO> factory = new
     * ConcurrentKafkaListenerContainerFactory<>();
     * factory.setConsumerFactory(dtoConsumerFactory());
     * return factory;
     * }
     */
}
