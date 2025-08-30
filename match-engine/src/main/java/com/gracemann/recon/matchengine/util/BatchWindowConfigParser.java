package com.gracemann.recon.matchengine.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gracemann.recon.matchengine.dto.BatchWindowConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to parse batch window configuration from JSON config snapshot.
 */
@Component
public class BatchWindowConfigParser {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Parses window configuration from JSON config snapshot.
     *
     * Expected JSON format:
     * {
     *   "batchWindow": {
     *     "windowStart": "2024-01-01T00:00:00",
     *     "windowEnd": "2024-01-31T23:59:59"
     *   }
     * }
     *
     * @param configSnapshot JSON configuration string
     * @return BatchWindowConfig with parsed start and end times
     * @throws RuntimeException if parsing fails
     */
    public BatchWindowConfig parseWindow(String configSnapshot) {
        try {
            JsonNode root = objectMapper.readTree(configSnapshot);
            JsonNode batchWindow = root.get("batchWindow");

            if (batchWindow == null) {
                throw new IllegalArgumentException("Missing 'batchWindow' in config snapshot");
            }

            String windowStartStr = batchWindow.get("windowStart").asText();
            String windowEndStr = batchWindow.get("windowEnd").asText();

            BatchWindowConfig config = new BatchWindowConfig();
            config.setWindowStart(LocalDateTime.parse(windowStartStr, FORMATTER));
            config.setWindowEnd(LocalDateTime.parse(windowEndStr, FORMATTER));

            return config;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse batch window config: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a default window config (last 30 days) if config is missing/invalid.
     */
    public BatchWindowConfig createDefaultWindow() {
        BatchWindowConfig config = new BatchWindowConfig();
        config.setWindowStart(LocalDateTime.now().minusDays(30));
        config.setWindowEnd(LocalDateTime.now());
        return config;
    }
}