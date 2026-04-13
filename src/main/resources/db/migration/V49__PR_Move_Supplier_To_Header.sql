-- V49: Purchase Requisition — Move suggested_supplier_id from lines to header

-- ============================================================
-- 1. Add suggested_supplier_id to PR header
-- ============================================================
ALTER TABLE pur_purchase_requisitions
    ADD COLUMN IF NOT EXISTS suggested_supplier_id BIGINT NULL
        COMMENT 'Suggested supplier for the entire requisition',
    ADD CONSTRAINT fk_pr_suggested_supplier
        FOREIGN KEY (suggested_supplier_id) REFERENCES parties(id);

-- ============================================================
-- 2. Migrate existing data: copy supplier from lines to header
--    (pick the first non-null suggestedSupplierId per header)
-- ============================================================
UPDATE pur_purchase_requisitions h
    JOIN (
        SELECT header_id, MIN(suggested_supplier_id) AS supplier_id
        FROM pur_purchase_requisition_lines
        WHERE suggested_supplier_id IS NOT NULL
        GROUP BY header_id
    ) l ON h.id = l.header_id
SET h.suggested_supplier_id = l.supplier_id;

-- ============================================================
-- 3. Remove suggested_supplier_id from PR lines
-- ============================================================
ALTER TABLE pur_purchase_requisition_lines
    DROP FOREIGN KEY IF EXISTS fk_prl_supplier,
    DROP COLUMN IF EXISTS suggested_supplier_id;
