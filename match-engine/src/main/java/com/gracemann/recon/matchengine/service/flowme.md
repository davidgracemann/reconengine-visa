    # Reconciliation Batch Processing Flow

    ```
    ┌─────────────┐
    │   Client    │
    └─────┬───────┘
        │ POST /api/batches/start
        │ {operator, config}
        ▼
    ┌─────────────────────────────────────┐
    │   ReconciliationBatchController     │
    ├─────────────────────────────────────┤
    │ 1. createNewBatch(operator, config) │
    └─────────────┬───────────────────────┘
                │
                ▼
    ┌─────────────────────────────────────┐
    │   ReconciliationBatchService        │
    ├─────────────────────────────────────┤
    │ createNewBatch()                    │────────┐
    └─────────────┬───────────────────────┘        │
                │                                │
                ▼                                │
    ┌─────────────────────────────────────┐        │
    │               DB                    │        │
    ├─────────────────────────────────────┤        │
    │ INSERT ReconciliationBatchControl   │◄───────┘
    │ - batchId (generated)               │
    │ - operator                          │
    │ - configSnapshot                    │
    │ - status: STARTED                   │
    │ - startTimestamp                    │
    └─────────────────────────────────────┘
                │
                │ return batchId
                ▼
    ┌─────────────────────────────────────┐
    │   ReconciliationBatchController     │
    ├─────────────────────────────────────┤
    │ 2. executeBatch(batchId) [ASYNC]    │
    └─────────────┬───────────────────────┘
                │
                ▼
    ┌─────────────────────────────────────┐
    │   ReconciliationBatchService        │
    ├─────────────────────────────────────┤
    │ executeBatch(batchId)               │
    └─────────────┬───────────────────────┘
                │
                ▼
        ┌─────────────────────────────────────────────────────────────────┐
        │                    ASYNC BATCH EXECUTION                        │
        ├─────────────────────────────────────────────────────────────────┤
        │                                                                 │
        │  Step 1: Parse Window Configuration                             │
        │  ┌─────────────────────────────────┐                            │
        │  │ BatchWindowConfigParser         │                            │
        │  │ parseWindowConfig(configSnapshot│                            │
        │  └──────────┬──────────────────────┘                            │
        │             │ returns                                           │
        │             ▼                                                   │
        │  ┌─────────────────────────────────┐                            │
        │  │ windowStart: LocalDateTime      │                            │
        │  │ windowEnd: LocalDateTime        │                            │
        │  └──────────┬──────────────────────┘                            │
        │             │                                                   │
        │             ▼                                                   │
        │  Step 2: Fetch Transactions & Build Window                     │
        │  ┌─────────────────────────────────┐                            │
        │  │ LedgerFetchService              │                            │
        │  │ fetchBankTransactions()         │                            │
        │  │ fetchSchemeTransactions()       │                            │
        │  │ buildLedgerBatchWindow()        │                            │
        │  └──────────┬──────────────────────┘                            │
        │             │                                                   │
        │     ┌───────┴───────┐                                           │
        │     │ [PARALLEL]    │                                           │
        │     ▼               ▼                                           │
        │ ┌─────────────┐ ┌─────────────────────┐                        │
        │ │BankSwitch   │ │SchemeSettlement     │                        │
        │ │TransactionL │ │TransactionLedgerRepo│                        │
        │ │edgerRepo    │ │                     │                        │
        │ │             │ │                     │                        │
        │ │findByTxnTim │ │findByTxnTimestamp   │                        │
        │ │estampBetween│ │Between()            │                        │
        │ └─────┬───────┘ └─────────┬───────────┘                        │
        │       │                   │                                    │
        │       │ List<BankTxn>     │ List<SchemeTxn>                    │
        │       ▼                   ▼                                    │
        │ ┌─────────────────────────────────────┐                        │
        │ │ LedgerFetchService                  │                        │
        │ │ buildLedgerBatchWindow(             │                        │
        │ │   windowStart,                      │                        │
        │ │   windowEnd,                        │                        │
        │ │   bankTxnList,                      │                        │
        │ │   schemeTxnList                     │                        │
        │ │ )                                   │                        │
        │ └──────────┬──────────────────────────┘                        │
        │            │ creates unified DTO                               │
        │            ▼                                                   │
        │ ┌─────────────────────────────────────┐                        │
        │ │ LedgerBatchWindow                   │                        │
        │ │ {                                   │                        │
        │ │   windowStart: LocalDateTime,       │                        │
        │ │   windowEnd: LocalDateTime,         │                        │
        │ │   bankTransactions: List<BankTxn>,  │                        │
        │ │   schemeTransactions: List<SchemeTxn│                        │
        │ │ }                                   │                        │
        │ └──────────┬──────────────────────────┘                        │
        │            │ windowData = ledgerBatchWindow                    │
        │            ▼                                                   │
        │  Step 3: Execute Matching Algorithm                             │
        │  ┌─────────────────────────────────┐                            │
        │  │ ReconciliationMatchingEngine    │                            │
        │  │ runMatching(LedgerBatchWindow)  │                            │
        │  └──────────┬──────────────────────┘                            │
        │             │ returns                                           │
        │             ▼                                                   │
        │  ┌─────────────────────────────────┐                            │
        │  │ BatchCounters                   │                            │
        │  │ {                               │                            │
        │  │   matchedCount,                 │                            │
        │  │   unmatchedBankCount,           │                            │
        │  │   unmatchedSchemeCount,         │                            │
        │  │   errorCount                    │                            │
        │  │ }                               │                            │
        │  └──────────┬──────────────────────┘                            │
        │             │                                                   │
        │             ▼                                                   │
        │  Step 4: Finalize Batch                                         │
        │  ┌─────────────────────────────────┐                            │
        │  │ completeBatch(batchId, counters)│                            │
        │  │        OR                       │                            │
        │  │ failBatch(batchId, error)       │                            │
        │  └──────────┬──────────────────────┘                            │
        │             │                                                   │
        │             ▼                                                   │
        │  ┌─────────────────────────────────┐                            │
        │  │ UPDATE DB                       │                            │
        │  │ ReconciliationBatchControl      │                            │
        │  │ SET status = COMPLETED/FAILED   │                            │
        │  │     endTimestamp = NOW()        │                            │
        │  │     matchedCount = ?            │                            │
        │  │     unmatchedBankCount = ?      │                            │
        │  │     unmatchedSchemeCount = ?    │                            │
        │  │     errorDetails = ?            │                            │
        │  │ WHERE batchId = ?               │                            │
        │  └─────────────────────────────────┘                            │
        │                                                                 │
        └─────────────────────────────────────────────────────────────────┘
                │
                ▼ (meanwhile, sync response)
    ┌─────────────────────────────────────┐
    │   ReconciliationBatchController     │
    ├─────────────────────────────────────┤
    │ Return HTTP 200:                    │
    │ {                                   │
    │   batchId: UUID,                    │
    │   status: "STARTED",                │
    │   startTimestamp: ISO8601           │
    │ }                                   │
    └─────────────┬───────────────────────┘
                │
                ▼
    ┌─────────────┐
    │   Client    │
    └─────────────┘
    ```

## Key Technical Details

### Database Schema
```sql
-- ReconciliationBatchControl Table
batchId (UUID, PK)
operator (VARCHAR)
configSnapshot (JSON)
status (ENUM: STARTED, COMPLETED, FAILED)
startTimestamp (TIMESTAMP)
endTimestamp (TIMESTAMP, nullable)
matchedCount (INTEGER, nullable)
unmatchedBankCount (INTEGER, nullable) 
unmatchedSchemeCount (INTEGER, nullable)
errorDetails (TEXT, nullable)
```

### Configuration Structure
```json
{
  "windowType": "FIXED_HOURS",
  "windowSize": 24,
  "offsetHours": 0,
  "matchingRules": {...}
}
```

### Transaction Repositories
- **BankSwitchTransactionLedgerRepo**: `findByTxnTimestampBetween(start, end)`
- **SchemeSettlementTransactionLedgerRepo**: `findByTxnTimestampBetween(start, end)`

### Processing Flow
1. **Synchronous Phase** (< 100ms): Batch creation and immediate response
2. **Asynchronous Phase** (minutes): Data fetching, matching, and completion
3. **Error Handling**: Any failure updates batch status to FAILED with error details