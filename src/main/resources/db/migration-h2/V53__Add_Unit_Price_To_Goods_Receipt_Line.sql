-- V53: Add unit_price to Goods Receipt Line (Sprint 4 Task 5 - Price Snapshotting)

ALTER TABLE pur_goods_receipt_lines 
ADD COLUMN unit_price DECIMAL(19,4) NOT NULL DEFAULT 0.0000;

-- Update existing records with a reasonable default (though in dev it might be zero)
UPDATE pur_goods_receipt_lines SET unit_price = 0.0000;
