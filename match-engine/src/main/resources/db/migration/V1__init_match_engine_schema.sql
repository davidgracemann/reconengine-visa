-- =====================================================================
-- MATCH ENGINE DATABASE SCHEMA INITIALIZATION (V1)
-- =====================================================================
--
-- PURPOSE: Core database schema for financial transaction reconciliation
-- SYSTEM: Match Engine - Payment Transaction Reconciliation Platform
-- VERSION: V1 - Initial Implementation
-- OWNER: Match Engine Team
-- COMPLIANCE: PCI-DSS Level 1, SOX, Audit-Ready
--
-- BUSINESS CONTEXT:
--   Supports real-time reconciliation between bank switch transactions
--   and card scheme settlement files, processing millions of daily transactions
--   with sub-second matching performance and comprehensive audit trails.
--
-- DATA VOLUME EXPECTATIONS:
--   - Bank Switch Transactions: ~2-5M daily
--   - Scheme Settlement Records: ~2-5M daily
--   - Match Results: ~4-10M daily
--   - Exception Tickets: ~50-500 daily
--   - Retention Period: 7 years (regulatory compliance)
-- =====================================================================

-- ========================================================
-- Enable extension for UUID generation (if not present)
-- ========================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================================================
-- BANK SWITCH TRANSACTION LEDGER
-- ========================================================
CREATE TABLE IF NOT EXISTS bank_switch_transaction_ledger (
    transaction_id VARCHAR(50) PRIMARY KEY,
    transaction_amount DECIMAL(18,2) NOT NULL,
    transaction_timestamp TIMESTAMP NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    merchant_identifier VARCHAR(50),
    masked_card_number VARCHAR(20),
    terminal_identifier VARCHAR(50),
    switch_response_code VARCHAR(10),
    transaction_channel VARCHAR(20),
    authorization_code VARCHAR(20),
    acquirer_batch_id VARCHAR(50),
    record_created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    raw_source_message TEXT,
    CONSTRAINT chk_transaction_amount CHECK (transaction_amount >= 0),
    CONSTRAINT chk_currency_code CHECK (LENGTH(currency_code) = 3)
);

-- ========================================================
-- SCHEME SETTLEMENT FILE LEDGER
-- ========================================================
CREATE TABLE IF NOT EXISTS scheme_settlement_transaction_ledger (
    transaction_id VARCHAR(50) PRIMARY KEY,
    settlement_amount DECIMAL(18,2) NOT NULL,
    transaction_timestamp TIMESTAMP NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    merchant_identifier VARCHAR(50),
    masked_card_number VARCHAR(20),
    terminal_identifier VARCHAR(50),
    scheme_response_code VARCHAR(10),
    scheme_settlement_batch_id VARCHAR(50) NOT NULL,
    card_scheme_name VARCHAR(20) NOT NULL,
    record_created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    raw_settlement_record TEXT,
    CONSTRAINT chk_settlement_amount CHECK (settlement_amount >= 0),
    CONSTRAINT chk_currency_code_scheme CHECK (LENGTH(currency_code) = 3),
    CONSTRAINT chk_card_scheme_name CHECK (card_scheme_name IN ('VISA', 'MASTERCARD', 'AMEX', 'RUPAY', 'DISCOVER'))
);

-- ========================================================
-- RECONCILIATION BATCH CONTROL
-- ========================================================
CREATE TABLE IF NOT EXISTS reconciliation_batch_control (
    batch_execution_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_start_timestamp TIMESTAMP NOT NULL,
    batch_end_timestamp TIMESTAMP,
    batch_execution_status VARCHAR(20) NOT NULL,
    total_transactions_processed INTEGER DEFAULT 0,
    exact_matches_found INTEGER DEFAULT 0,
    fuzzy_matches_found INTEGER DEFAULT 0,
    unmatched_bank_transactions INTEGER DEFAULT 0,
    unmatched_scheme_transactions INTEGER DEFAULT 0,
    exception_cases_generated INTEGER DEFAULT 0,
    batch_created_by VARCHAR(50) NOT NULL,
    batch_configuration_snapshot JSONB,
    batch_created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_batch_status CHECK (batch_execution_status IN
        ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_processing_metrics CHECK (
        total_transactions_processed >= (exact_matches_found + fuzzy_matches_found +
        unmatched_bank_transactions + unmatched_scheme_transactions))
);

-- ========================================================
-- TRANSACTION MATCH RESULTS
-- ========================================================
CREATE TABLE IF NOT EXISTS transaction_match_results (
    batch_execution_id UUID NOT NULL,
    primary_transaction_id VARCHAR(50) NOT NULL,
    bank_transaction_id VARCHAR(50),
    scheme_transaction_id VARCHAR(50),
    match_result_status VARCHAR(30) NOT NULL,
    overall_match_confidence DECIMAL(5,4),
    amount_variance DECIMAL(18,2),
    timestamp_variance_seconds BIGINT,
    merchant_name_similarity DECIMAL(5,4),
    match_processed_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    matching_algorithm_version VARCHAR(20) DEFAULT 'v1.0',
    PRIMARY KEY (batch_execution_id, primary_transaction_id),
    CONSTRAINT chk_match_confidence CHECK (overall_match_confidence BETWEEN 0.0000 AND 1.0000),
    CONSTRAINT chk_merchant_similarity CHECK (merchant_name_similarity BETWEEN 0.0000 AND 1.0000),
    CONSTRAINT chk_match_status CHECK (match_result_status IN (
        'EXACT_MATCH', 'FUZZY_MATCH', 'UNMATCHED_BANK', 'UNMATCHED_SCHEME',
        'MULTIPLE_MATCHES', 'CONFLICTED_MATCH', 'EXCEPTION_REQUIRED')),
    FOREIGN KEY (batch_execution_id) REFERENCES reconciliation_batch_control(batch_execution_id) ON DELETE CASCADE
);

-- ========================================================
-- RECONCILIATION DISCREPANCY LOG
-- ========================================================
CREATE TABLE IF NOT EXISTS reconciliation_discrepancy_log (
    discrepancy_log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    related_batch_id UUID,
    affected_transaction_id VARCHAR(50),
    discrepancy_category VARCHAR(50) NOT NULL,
    business_impact_severity VARCHAR(20) NOT NULL,
    discrepancy_description TEXT NOT NULL,
    technical_details JSONB,
    discrepancy_detected_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_severity_level CHECK (business_impact_severity IN
        ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_discrepancy_category CHECK (discrepancy_category IN (
        'AMOUNT_MISMATCH', 'TIMESTAMP_DEVIATION', 'MISSING_BANK_TRANSACTION',
        'MISSING_SCHEME_TRANSACTION', 'DUPLICATE_TRANSACTION', 'MERCHANT_MISMATCH',
        'CURRENCY_INCONSISTENCY', 'CARD_NUMBER_MISMATCH', 'MULTIPLE_MATCH_CONFLICT')),
    FOREIGN KEY (related_batch_id) REFERENCES reconciliation_batch_control(batch_execution_id) ON DELETE CASCADE
);

-- ========================================================
-- EXCEPTION MANAGEMENT TICKETS
-- ========================================================
CREATE TABLE IF NOT EXISTS exception_management_tickets (
    exception_ticket_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    originating_batch_id UUID NOT NULL,
    problem_transaction_id VARCHAR(50) NOT NULL,
    exception_category VARCHAR(50) NOT NULL,
    business_priority VARCHAR(20) DEFAULT 'MEDIUM',
    exception_summary TEXT NOT NULL,
    detailed_investigation_notes TEXT,
    assigned_to_analyst VARCHAR(50),
    current_ticket_status VARCHAR(20) DEFAULT 'OPEN',
    resolution_summary TEXT,
    root_cause_analysis TEXT,
    ticket_created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ticket_resolved_timestamp TIMESTAMP,
    last_updated_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_business_priority CHECK (business_priority IN
        ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_ticket_status CHECK (current_ticket_status IN
        ('OPEN', 'IN_PROGRESS', 'PENDING_INFO', 'RESOLVED', 'CLOSED', 'ESCALATED')),
    CONSTRAINT chk_exception_category CHECK (exception_category IN (
        'RECONCILIATION_FAILURE', 'DATA_QUALITY_ISSUE', 'AMOUNT_DISCREPANCY',
        'TIMING_MISMATCH', 'MERCHANT_DATA_ISSUE', 'SCHEME_FILE_PROBLEM',
        'SWITCH_DATA_CORRUPTION', 'CONFIGURATION_ERROR', 'SYSTEM_ANOMALY')),
    FOREIGN KEY (originating_batch_id) REFERENCES reconciliation_batch_control(batch_execution_id) ON DELETE RESTRICT
);

-- ========================================================
-- MATCHING TOLERANCE CONFIGURATION
-- ========================================================
CREATE TABLE IF NOT EXISTS fuzzy_matching_tolerance_rules (
    tolerance_rule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    matching_field_name VARCHAR(50) NOT NULL,
    tolerance_calculation_type VARCHAR(20) NOT NULL,
    threshold_value DECIMAL(18,4) NOT NULL,
    tolerance_description TEXT,
    rule_enabled_flag BOOLEAN DEFAULT TRUE,
    effective_from_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    effective_until_date TIMESTAMP,
    rule_created_by VARCHAR(50) NOT NULL,
    business_justification TEXT,
    rule_created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rule_last_modified_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tolerance_type CHECK (tolerance_calculation_type IN
        ('ABSOLUTE', 'PERCENTAGE', 'TEMPORAL_SECONDS', 'SIMILARITY_SCORE')),
    CONSTRAINT chk_threshold_positive CHECK (threshold_value > 0),
    CONSTRAINT chk_effective_date_range CHECK (
        effective_until_date IS NULL OR effective_until_date > effective_from_date)
);

-- ========================================================
-- HIGH-PERFORMANCE INDEXES (non-concurrent for V1 migration)
-- ========================================================

CREATE INDEX IF NOT EXISTS idx_bank_switch_timestamp_range
    ON bank_switch_transaction_ledger (transaction_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_bank_switch_merchant_lookup
    ON bank_switch_transaction_ledger (merchant_identifier, transaction_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_bank_switch_batch_processing
    ON bank_switch_transaction_ledger (acquirer_batch_id, record_created_timestamp);

CREATE INDEX IF NOT EXISTS idx_scheme_settlement_timestamp_range
    ON scheme_settlement_transaction_ledger (transaction_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_scheme_settlement_batch_lookup
    ON scheme_settlement_transaction_ledger (scheme_settlement_batch_id, card_scheme_name);

CREATE INDEX IF NOT EXISTS idx_scheme_settlement_scheme_analysis
    ON scheme_settlement_transaction_ledger (card_scheme_name, record_created_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_match_results_status_analysis
    ON transaction_match_results (match_result_status, batch_execution_id);

CREATE INDEX IF NOT EXISTS idx_match_results_confidence_analysis
    ON transaction_match_results (overall_match_confidence DESC, match_processed_timestamp);

CREATE INDEX IF NOT EXISTS idx_exception_tickets_workload
    ON exception_management_tickets (current_ticket_status, business_priority, assigned_to_analyst);

CREATE INDEX IF NOT EXISTS idx_exception_tickets_sla_monitoring
    ON exception_management_tickets (business_priority, ticket_created_timestamp DESC)
    WHERE current_ticket_status IN ('OPEN', 'IN_PROGRESS', 'PENDING_INFO');

CREATE INDEX IF NOT EXISTS idx_discrepancy_log_trending
    ON reconciliation_discrepancy_log (discrepancy_category, business_impact_severity, discrepancy_detected_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_tolerance_rules_active_lookup
    ON fuzzy_matching_tolerance_rules (matching_field_name, rule_enabled_flag)
    WHERE rule_enabled_flag = TRUE;

-- ========================================================
-- AUDIT TRAIL & COMPLIANCE ENHANCEMENTS
-- ========================================================

-- Trigger to update last_updated_timestamp on exception tickets
CREATE OR REPLACE FUNCTION update_exception_ticket_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_updated_timestamp = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_exception_ticket_update_timestamp
    BEFORE UPDATE ON exception_management_tickets
    FOR EACH ROW
    EXECUTE FUNCTION update_exception_ticket_timestamp();

-- ========================================================
-- END OF MIGRATION V1
-- ========================================================
