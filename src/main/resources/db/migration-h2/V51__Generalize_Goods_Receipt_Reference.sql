ALTER TABLE pur_goods_receipts ADD COLUMN reference_type VARCHAR(40) NULL;
ALTER TABLE pur_goods_receipts ADD COLUMN reference_id BIGINT NULL;

UPDATE pur_goods_receipts
SET reference_type = 'PURCHASE_ORDER',
    reference_id = po_id
WHERE reference_type IS NULL
  AND reference_id IS NULL;

ALTER TABLE pur_goods_receipts
    ALTER COLUMN reference_type SET DATA TYPE VARCHAR(40);

ALTER TABLE pur_goods_receipts
    ALTER COLUMN reference_type SET NOT NULL;

ALTER TABLE pur_goods_receipts
    ALTER COLUMN reference_id SET DATA TYPE BIGINT;

ALTER TABLE pur_goods_receipts
    ALTER COLUMN reference_id SET NOT NULL;

CREATE INDEX idx_gr_reference_type_id ON pur_goods_receipts(reference_type, reference_id);

ALTER TABLE pur_goods_receipts
    DROP CONSTRAINT fk_gr_po;

ALTER TABLE pur_goods_receipts
    DROP COLUMN po_id;
