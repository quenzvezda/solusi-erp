-- =====================================================
-- V44: Widen status column for NEVER_OPENED and add period_number
-- =====================================================

-- Widen status column to accommodate NEVER_OPENED (12 chars)
ALTER TABLE acc_accounting_periods
    MODIFY COLUMN status VARCHAR(15) NOT NULL DEFAULT 'NEVER_OPENED' COMMENT 'NEVER_OPENED | OPEN | CLOSED';

-- Add period_number column (1-12 for monthly periods)
ALTER TABLE acc_accounting_periods
    ADD COLUMN period_number INT NOT NULL DEFAULT 0 AFTER name;
