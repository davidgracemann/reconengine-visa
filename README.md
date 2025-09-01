# Financial Reconciliation Engine

> useCase = VISA Schemes Vs Bank GL 

## Project Status
**Current Phase**: Pre-Production Testing & Integration

The core matching and ingestion engine is fully implemented. The following components are under active development:

### In Progress
- **Reporting & Exception API**: BankOps integration pending
- **Monitoring Infrastructure**: Grafana dashboard integration pending  
- **Security Hardening**: Non-functional requirements and TestOps validation pending

## Performance Specifications

### Throughput & Latency
- **Sustained Throughput**: 333 TPS (10,000 transactions per 30-minute batch window)
- **Processing Latency**: P95 < 50ms, P99 < 100ms
- **Batch Initialization**: < 100ms synchronous response
- **Memory Footprint**: JVM heap < 2GB under peak load

### Reliability & Data Integrity
- **Batch Completion Rate**: 99.5% automated success (< 0.5% manual intervention required)
- **Data Consistency**: 100% ACID compliance across dual-database architecture
- **Message Delivery**: Zero-loss guarantee with Kafka at-least-once semantics
- **Recovery Objectives**: RTO < 60 seconds, RPO = 0 (point-in-time recovery)

### Scalability & Performance
- **Horizontal Scaling**: Support for 5+ concurrent batch workers
- **Database Connections**: Optimized pooling (HikariCP) with maximum 20 connections
- **Stream Processing**: Kafka consumer lag < 1 second during peak ingestion
- **Algorithm Complexity**: Lock-free matching with O(n log n) time complexity

### Observability & Monitoring
- **Metrics Collection**: Micrometer → Prometheus (15-second scrape interval)
- **Monitoring Uptime**: 99.9% metric collection availability
- **Incident Response**: Alert propagation < 30 seconds
- **Distributed Tracing**: Spring Cloud Sleuth integration

## Business Problem

Financial institutions process millions of card authorization transactions daily through their switch ledgers, while payment schemes (Visa, RuPay) clear and settle these same transactions through separate daily settlement files. Transaction mismatches represent potential revenue leakage and regulatory compliance risks.

This reconciliation engine automates the matching process by:
- Ingesting dual transaction feeds in real-time
- Applying hash-based and fuzzy logic matching algorithms
- Surfacing discrepancies for immediate resolution
- Transforming manual reconciliation processes into auditable, SLA-backed services

## System Architecture

```
recon-engine/               ← Git repository root
│  
├─ pom.xml                  ← Parent POM configuration
│  
├─ ingestion-service/       ← Transaction data ingestion module
│   └─ pom.xml              
│  
├─ match-engine/            ← Core reconciliation matching logic
│   └─ pom.xml
│  
├─ report-service/          ← Reporting and analytics module
│   └─ pom.xml
│  
├─ exception-api/           ← Exception handling and manual resolution
│   └─ pom.xml
│  
├─ scheduler/               ← Batch scheduling and orchestration
│   └─ pom.xml
│  
└─ monitor-metrics/         ← Observability and metrics collection
    └─ pom.xml
```

## Technology Stack

| **Technology** | **Purpose** |
|---|---|
| **Java (JDK 17+)** | Primary application development language |
| **Spring Boot** | Microservices framework, REST APIs, security, scheduling |
| **Apache Kafka** | High-volume transaction data streaming and ingestion |
| **CSV File Processing** | Rapid prototyping and file-based transaction import |
| **Apache Cassandra** | High-performance storage for transaction matching results |
| **PostgreSQL** | Relational storage for exceptions, resolutions, and audit data |
| **MinIO** | S3-compatible object storage for reports and file artifacts |
| **JUnit** | Unit testing framework for code validation |
| **k6** | Load testing and performance validation |
| **JMH** | Micro-benchmarking for critical performance paths |
| **Micrometer** | Application metrics collection and instrumentation |
| **Prometheus** | Time-series metrics storage and monitoring |
| **Grafana** | Metrics visualization and operational dashboards |
| **OWASP ZAP** | Security vulnerability scanning for REST APIs |
| **Spring Security** | Authentication, authorization, and HTTPS implementation |

All components are open-source and deploy natively on Windows environments without containerization dependencies.

## Processing Architecture

### Batch Processing Flow

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

**Client Request**
```
POST /api/batches/start
{
  "operator": "string",
  "config": {...}
}
```

**Synchronous Phase** (< 100ms)
1. **Batch Creation**: Generate unique batch identifier
2. **Configuration Validation**: Parse and validate window parameters
3. **Status Persistence**: Insert batch control record with STARTED status
4. **Immediate Response**: Return batch ID and timestamp

**Asynchronous Phase** (minutes)
1. **Window Calculation**: Parse time window from configuration
2. **Parallel Data Fetch**: Query bank and scheme transaction repositories
3. **Ledger Window Construction**: Build unified transaction view
4. **Matching Execution**: Run reconciliation algorithm
5. **Batch Finalization**: Update status and counters

### Database Schema

```sql
-- Batch Control Table
CREATE TABLE ReconciliationBatchControl (
    batchId UUID PRIMARY KEY,
    operator VARCHAR(255) NOT NULL,
    configSnapshot JSONB NOT NULL,
    status VARCHAR(20) CHECK (status IN ('STARTED', 'COMPLETED', 'FAILED')),
    startTimestamp TIMESTAMP NOT NULL,
    endTimestamp TIMESTAMP,
    matchedCount INTEGER,
    unmatchedBankCount INTEGER,
    unmatchedSchemeCount INTEGER,
    errorDetails TEXT
);
```

### Configuration Schema

```json
{
  "windowType": "FIXED_HOURS",
  "windowSize": 24,
  "offsetHours": 0,
  "matchingRules": {
    "algorithm": "HASH_FUZZY",
    "tolerance": 0.95
  }
}
```

### Repository Interfaces

- **BankSwitchTransactionLedgerRepo**: `findByTxnTimestampBetween(LocalDateTime start, LocalDateTime end)`
- **SchemeSettlementTransactionLedgerRepo**: `findByTxnTimestampBetween(LocalDateTime start, LocalDateTime end)`

## Error Handling

The system implements comprehensive error handling with automatic batch status management:
- **Validation Failures**: Configuration errors result in immediate rejection
- **Processing Errors**: Runtime exceptions update batch status to FAILED with detailed error context
- **Timeout Handling**: Long-running operations include circuit breaker patterns
- **Data Integrity**: All state changes are transactionally consistent
