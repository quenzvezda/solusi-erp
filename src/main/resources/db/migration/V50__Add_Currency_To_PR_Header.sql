-- V50: Purchase Requisition — Add currency_id to header

-- ============================================================
-- 1. Add currency_id to PR header
-- ============================================================
ALTER TABLE pur_purchase_requisitions
    ADD COLUMN IF NOT EXISTS currency_id BIGINT NULL
        COMMENT 'Currency for estimated unit prices in this requisition',
    ADD CONSTRAINT fk_pr_currency
        FOREIGN KEY (currency_id) REFERENCES master_currencies(id);

-- ============================================================
-- 2. Set default currency to IDR (id=1) for existing PRs
--    (Note: adjust id=1 if IDR has different ID in your system)
-- ============================================================
UPDATE pur_purchase_requisitions
SET currency_id = (
    SELECT id FROM master_currencies WHERE code = 'IDR' LIMIT 1
)
WHERE currency_id IS NULL;

-- ============================================================
-- 3. Add NOT NULL constraint after data migration
-- ============================================================
ALTER TABLE pur_purchase_requisitions
    MODIFY COLUMN currency_id BIGINT NOT NULL;
