ALTER TABLE pur_goods_receipt_lines
    DROP CONSTRAINT fk_grl_po_line;

ALTER TABLE pur_goods_receipt_lines
    ALTER COLUMN po_line_id RENAME TO reference_line_id;

ALTER TABLE pur_goods_receipt_lines
    ADD COLUMN is_serialized BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE pur_goods_receipt_lines
    ADD CONSTRAINT fk_grl_reference_line
        FOREIGN KEY (reference_line_id) REFERENCES pur_purchase_order_lines(id);
