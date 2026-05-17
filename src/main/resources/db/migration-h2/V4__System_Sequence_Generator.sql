-- V4: System Sequence Generator (Generic)
-- Mandate: AGENTS.md Section 4

CREATE TABLE system_sequences (
    module_code VARCHAR(50) PRIMARY KEY,
    format_pattern VARCHAR(100) NOT NULL,
    current_value BIGINT NOT NULL DEFAULT 0,
    pad_length INT NOT NULL DEFAULT 4,
    reset_cycle ENUM('DAILY', 'MONTHLY', 'YEARLY', 'NEVER') NOT NULL DEFAULT 'NEVER',
    last_reset_date DATETIME,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
);

-- Initial Seed for Product Category
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by, updated_date)
VALUES ('PRODUCT_CATEGORY', 'PCAT-{seq}', 4, 'NEVER', 'SYSTEM', NOW());
