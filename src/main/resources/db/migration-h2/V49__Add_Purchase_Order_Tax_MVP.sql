ALTER TABLE pur_purchase_orders ADD COLUMN IF NOT EXISTS tax_id BIGINT NULL;
ALTER TABLE pur_purchase_orders ADD COLUMN IF NOT EXISTS tax_code VARCHAR(50) NULL;
ALTER TABLE pur_purchase_orders ADD COLUMN IF NOT EXISTS tax_name VARCHAR(150) NULL;
ALTER TABLE pur_purchase_orders ADD COLUMN IF NOT EXISTS tax_rate DECIMAL(10,4) NOT NULL DEFAULT 0;
ALTER TABLE pur_purchase_orders ADD COLUMN IF NOT EXISTS tax_calculation_mode VARCHAR(20) NOT NULL DEFAULT 'EXCLUSIVE';

UPDATE pur_purchase_orders SET
    tax_rate = COALESCE((
        SELECT CASE WHEN MIN(pol.tax_rate) = MAX(pol.tax_rate) THEN MAX(pol.tax_rate) ELSE NULL END
        FROM pur_purchase_order_lines pol WHERE pol.header_id = pur_purchase_orders.id
    ), 0),
    tax_calculation_mode = 'EXCLUSIVE'
WHERE tax_rate = 0;
