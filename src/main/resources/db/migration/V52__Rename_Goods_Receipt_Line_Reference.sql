ALTER TABLE pur_goods_receipt_lines
    DROP FOREIGN KEY fk_grl_po_line;

ALTER TABLE pur_goods_receipt_lines
    CHANGE COLUMN po_line_id reference_line_id BIGINT NOT NULL,
    ADD COLUMN is_serialized BOOLEAN NOT NULL DEFAULT FALSE AFTER product_id;

ALTER TABLE pur_goods_receipt_lines
    ADD CONSTRAINT fk_grl_reference_line
        FOREIGN KEY (reference_line_id) REFERENCES pur_purchase_order_lines(id);
