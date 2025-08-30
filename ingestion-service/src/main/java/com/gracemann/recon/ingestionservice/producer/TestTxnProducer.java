package com.gracemann.recon.ingestionservice.producer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gracemann.recon.ingestionservice.config.KafkaConfig;

/**
 * TestTxnProducer simulates ingestion of raw bank ISO8583-like messages and
 * scheme CSV lines INCLUDING INTENTIONALLY BAD DATA for recon engine testing.
 */
public class TestTxnProducer {

    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter SLASH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss");

    // Standalone mapper, not Spring-injected
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new JavaTimeModule());
    }

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(KafkaConfig.class);

        try {
            KafkaTemplate<String, String> kafkaTemplate = context.getBean("stringKafkaTemplate", KafkaTemplate.class);

            String bankTopic = "bank-txn-topic";
            String schemeTopic = "scheme-txn-topic";

            // Send bank messages
            for (String isoRaw : RawIsoTestMessages.ALL_MESSAGES) {
                try {
                    String txnId = extractField(isoRaw, "txnId");
                    String json = parseIsoToJson(isoRaw);
                    kafkaTemplate.send(bankTopic, txnId, json);
                    System.out.println("Sent bank message with txnId=" + txnId);
                } catch (Exception e) {
                    String fallbackTxnId = "PARSE_ERROR_" + System.currentTimeMillis();
                    String errorJson = createErrorRecord(isoRaw, "BANK_SWITCH", e.getMessage());
                    kafkaTemplate.send(bankTopic, fallbackTxnId, errorJson);
                    System.out.println("Sent error bank message: " + e.getMessage());
                }
            }

            // Send scheme messages
            for (String csvRaw : RawCsvTestMessages.ALL_MESSAGES) {
                if (csvRaw == null || csvRaw.trim().isEmpty() || csvRaw.startsWith("date,")) {
                    continue;
                }

                try {
                    String txnId = extractTxnIdFromCsv(csvRaw);
                    String json = parseCsvToJson(csvRaw);
                    if (json != null) {
                        kafkaTemplate.send(schemeTopic, txnId, json);
                        System.out.println("Sent scheme message with txnId=" + txnId);
                    } else {
                        String errorJson = createErrorRecord(csvRaw, "SCHEME_FILE", "Failed CSV parsing");
                        kafkaTemplate.send(schemeTopic, txnId, errorJson);
                        System.out.println("Sent error scheme message with txnId=" + txnId);
                    }
                } catch (Exception e) {
                    String fallbackTxnId = "CSV_PARSE_ERROR_" + System.currentTimeMillis();
                    String errorJson = createErrorRecord(csvRaw, "SCHEME_FILE", e.getMessage());
                    kafkaTemplate.send(schemeTopic, fallbackTxnId, errorJson);
                    System.out.println("Sent exception scheme message: " + e.getMessage());
                }
            }
        } finally {
            context.close();
        }
    }

    // --- All helper methods below use the static mapper ---

    private static String createErrorRecord(String rawData, String sourceType, String error) throws Exception {
        ObjectNode node = mapper.createObjectNode();
        node.put("txnId", "ERROR_" + System.currentTimeMillis());
        node.putNull("cardNumber");
        node.putNull("amount");
        node.putNull("txnTimestamp");
        node.putNull("currency");
        node.putNull("merchantId");
        node.putNull("terminalId");
        node.putNull("responseCode");
        node.putNull("channel");
        node.putNull("batchId");
        node.putNull("schemeName");
        node.putNull("authCode");
        node.put("sourceType", sourceType);
        node.put("rawSourceRecord", rawData);
        node.put("parseError", error);

        return mapper.writeValueAsString(node);
    }

    public static String parseIsoToJson(String isoRaw) throws Exception {
        if (isoRaw == null || isoRaw.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty ISO message");
        }

        Map<String, String> fields = parseKeyValuePairs(isoRaw, "\\|");
        ObjectNode node = mapper.createObjectNode();

        node.put("txnId", fields.getOrDefault("txnId", "MISSING_TXN_ID"));
        node.put("cardNumber", fields.getOrDefault("cardNumber", null));
        node.put("currency", fields.getOrDefault("currency", null));
        node.put("merchantId", fields.getOrDefault("merchantId", null));
        node.put("terminalId", fields.getOrDefault("terminalId", null));
        node.put("responseCode", fields.getOrDefault("responseCode", null));
        node.put("channel", fields.getOrDefault("channel", null));
        node.putNull("batchId");
        node.putNull("schemeName");
        node.put("authCode", fields.getOrDefault("authCode", null));
        node.put("sourceType", "BANK_SWITCH");
        node.put("rawSourceRecord", isoRaw);

        String amountStr = fields.get("amount");
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                node.put("amount", new BigDecimal(amountStr));
            } catch (NumberFormatException e) {
                node.put("amount", amountStr);
            }
        } else {
            node.putNull("amount");
        }

        String timestamp = fields.get("txnTimestamp");
        if (timestamp != null && !timestamp.isEmpty()) {
            node.put("txnTimestamp", timestamp);
        } else {
            node.putNull("txnTimestamp");
        }

        return mapper.writeValueAsString(node);
    }

    public static String parseCsvToJson(String csvRaw) throws Exception {
        if (csvRaw == null || csvRaw.trim().isEmpty()) {
            return null;
        }

        String[] parts = csvRaw.split(",", -1);

        ObjectNode node = mapper.createObjectNode();

        String dateStr = parts.length > 0 ? parts[0] : "";
        String schemeName = parts.length > 1 ? parts[1] : "";
        String txnId = parts.length > 2 ? parts[2] : "MISSING_TXN_ID";
        String cardNumber = parts.length > 3 ? parts[3] : "";
        String amountStr = parts.length > 4 ? parts[4] : "";
        String currency = parts.length > 5 ? parts[5] : "";
        String merchantId = parts.length > 6 ? parts[6] : "";
        String terminalId = parts.length > 7 ? parts[7] : "";
        String responseCode = parts.length > 8 ? parts[8] : "";
        String batchId = parts.length > 9 ? parts[9] : "";

        node.put("txnId", txnId);
        node.put("cardNumber", cardNumber.isEmpty() ? null : cardNumber);
        node.put("currency", currency.isEmpty() ? null : currency);
        node.put("merchantId", merchantId.isEmpty() ? null : merchantId);
        node.put("terminalId", terminalId.isEmpty() ? null : terminalId);
        node.put("responseCode", responseCode.isEmpty() ? null : responseCode);
        node.put("batchId", batchId.isEmpty() ? null : batchId);
        node.put("schemeName", schemeName.isEmpty() ? null : schemeName);
        node.putNull("channel");
        node.putNull("authCode");
        node.put("sourceType", "SCHEME_FILE");
        node.put("rawSourceRecord", csvRaw);

        if (!amountStr.isEmpty()) {
            try {
                node.put("amount", new BigDecimal(amountStr));
            } catch (NumberFormatException e) {
                node.put("amount", amountStr);
            }
        } else {
            node.putNull("amount");
        }

        // Date parsing logic handles both yyyy-MM-dd and dd/MM/yyyy
        if (!dateStr.isEmpty()) {
            boolean parsed = false;
            // Try yyyy-MM-dd
            try {
                LocalDateTime txnTimestamp = LocalDateTime.parse(dateStr + "T00:00:00");
                node.put("txnTimestamp", txnTimestamp.toString());
                parsed = true;
            } catch (Exception ignored) {
            }
            // Try dd/MM/yyyy
            if (!parsed) {
                try {
                    LocalDateTime txnTimestamp = LocalDateTime.parse(dateStr + "T00:00:00", SLASH_DATE_FORMAT);
                    node.put("txnTimestamp", txnTimestamp.toString());
                    parsed = true;
                } catch (Exception ignored2) {
                }
            }
            // Fallback: raw string if unparseable
            if (!parsed) {
                node.put("txnTimestamp", dateStr);
            }
        } else {
            node.putNull("txnTimestamp");
        }

        if (parts.length < 7) {
            System.out.println("Processing short CSV record (" + parts.length + " fields): " + csvRaw);
        }

        return mapper.writeValueAsString(node);
    }

    private static Map<String, String> parseKeyValuePairs(String input, String delimiterRegex) {
        Map<String, String> map = new HashMap<>();
        if (input == null || input.trim().isEmpty()) {
            return map;
        }

        String[] pairs = input.split(delimiterRegex);
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    private static String extractField(String isoRaw, String key) {
        Map<String, String> map = parseKeyValuePairs(isoRaw, "\\|");
        return map.getOrDefault(key, "UNKNOWN");
    }

    private static String extractTxnIdFromCsv(String csvRaw) {
        if (csvRaw == null || csvRaw.trim().isEmpty()) {
            return "UNKNOWN";
        }

        String[] parts = csvRaw.split(",", -1);
        if (parts.length > 2) {
            return parts[2].isEmpty() ? "EMPTY_TXN_ID" : parts[2];
        }
        return "SHORT_CSV_RECORD";
    }
}
