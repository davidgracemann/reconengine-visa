package com.gracemann.recon.matchengine.controller;

import com.gracemann.recon.matchengine.domain.ReconciliationBatchControl;
import com.gracemann.recon.matchengine.service.ReconciliationBatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for triggering reconciliation batch execution.
 */
@RestController
@RequestMapping("/api/batches")
public class ReconciliationBatchController {

    private final ReconciliationBatchService batchService;

    public ReconciliationBatchController(ReconciliationBatchService batchService) {
        this.batchService = batchService;
    }

    /**
     * Starts a new reconciliation batch and executes it asynchronously.
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startBatch(@RequestBody(required = false) StartBatchRequest request) {
        String operator = (request != null && request.operator() != null && !request.operator().isBlank())
                ? request.operator()
                : "SYSTEM";
        String config = (request != null) ? request.configSnapshot() : null;

        // Step 1: Create the batch record
        ReconciliationBatchControl batch = batchService.createNewBatch(operator, config);

        // Step 2: Execute the batch asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                batchService.executeBatch(batch.getBatchExecutionId());
            } catch (Exception e) {
                // Error handling is done inside executeBatch
                System.err.println("Batch execution failed: " + e.getMessage());
            }
        });

        // Step 3: Return immediate response
        Map<String, Object> response = new HashMap<>();
        response.put("batchExecutionId", batch.getBatchExecutionId());
        response.put("status", batch.getBatchExecutionStatus());
        response.put("batchStartTimestamp", batch.getBatchStartTimestamp());
        response.put("message", "Batch started successfully and is processing in background");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Test endpoint to verify ledger fetching works.
     */
    @PostMapping("/test-fetch")
    public ResponseEntity<Map<String, Object>> testFetch() {
        try {
            batchService.testLedgerFetch();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test fetch completed successfully. Check console logs for results.");
            response.put("status", "SUCCESS");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test fetch failed: " + e.getMessage());
            response.put("status", "FAILED");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get batch status by ID.
     */
    @GetMapping("/{batchId}")
    public ResponseEntity<ReconciliationBatchControl> getBatch(@PathVariable("batchId") String batchId) {
        try {
            return batchService.getBatchById(java.util.UUID.fromString(batchId))
                    .map(batch -> ResponseEntity.ok(batch))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Simple DTO for start batch request.
     */
    public record StartBatchRequest(
            String operator,
            String configSnapshot) {
    }
}