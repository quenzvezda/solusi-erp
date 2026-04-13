-- V50: Purchase Requisition — Add currency_id to header

-- ============================================================
-- 1. Drop existing FK if it exists (for idempotency)
-- ============================================================
ALTER TABLE pur_purchase_requisitions DROP FOREIGN KEY IF EXISTS fk_pr_currency;

-- ============================================================
-- 2. Add currency_id column if it doesn't exist
-- ============================================================
ALTER TABLE pur_purchase_requisitions
    ADD COLUMN IF NOT EXISTS currency_id BIGINT NULL
        COMMENT 'Currency for estimated unit prices in this requisition';

-- ============================================================
-- 3. Recreate the FK constraint
-- ============================================================
ALTER TABLE pur_purchase_requisitions
    ADD CONSTRAINT fk_pr_currency
        FOREIGN KEY (currency_id) REFERENCES master_currencies(id);

-- ============================================================
-- 4. Set default currency for existing PRs without currency_id
-- ============================================================
UPDATE pur_purchase_requisitions
SET currency_id = COALESCE(
    (SELECT id FROM master_currencies WHERE is_active = true ORDER BY id LIMIT 1),
    1
)
WHERE currency_id IS NULL;

-- ============================================================
-- 5. Add NOT NULL constraint after data migration
-- ============================================================
ALTER TABLE pur_purchase_requisitions
    MODIFY COLUMN currency_id BIGINT NOT NULL;
