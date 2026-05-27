-- V40: Add sequence code to News + reference_code to Approval Requests
-- Safe/idempotent: uses IF NOT EXISTS for all DDL

-- 1. Add code column to common_news (only if not already present)
ALTER TABLE common_news ADD COLUMN IF NOT EXISTS code VARCHAR(50) NOT NULL DEFAULT '';

-- 2. Backfill existing rows with a placeholder code (if any)
UPDATE common_news SET code = CONCAT('NEWS-MIGR-', id) WHERE code = '' OR code IS NULL;

-- 3. Remove default and add unique constraint (safe via IF NOT EXISTS)
ALTER TABLE common_news ALTER COLUMN code DROP DEFAULT;
CREATE UNIQUE INDEX IF NOT EXISTS uk_news_code ON common_news(code);

-- 4. Add NEWS sequence to system_sequences
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('NEWS', 'NEWS-{seq}', 4, 'NEVER', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- 5. Add reference_code to appr_requests (only if not already present)
ALTER TABLE appr_requests ADD COLUMN IF NOT EXISTS reference_code VARCHAR(100);
