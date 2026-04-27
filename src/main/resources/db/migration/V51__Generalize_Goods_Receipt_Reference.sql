ALTER TABLE pur_goods_receipts
    ADD COLUMN reference_type VARCHAR(40) NULL AFTER receipt_date,
    ADD COLUMN reference_id BIGINT NULL AFTER reference_type;

UPDATE pur_goods_receipts
SET reference_type = 'PURCHASE_ORDER',
    reference_id = po_id
WHERE reference_type IS NULL
  AND reference_id IS NULL;

ALTER TABLE pur_goods_receipts
    MODIFY COLUMN reference_type VARCHAR(40) NOT NULL,
    MODIFY COLUMN reference_id BIGINT NOT NULL;

CREATE INDEX idx_gr_reference_type_id ON pur_goods_receipts(reference_type, reference_id);

ALTER TABLE pur_goods_receipts
    DROP FOREIGN KEY fk_gr_po;

ALTER TABLE pur_goods_receipts
    DROP COLUMN po_id;
