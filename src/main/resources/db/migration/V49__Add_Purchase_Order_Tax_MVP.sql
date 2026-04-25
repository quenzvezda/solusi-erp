ALTER TABLE pur_purchase_orders
    ADD COLUMN IF NOT EXISTS tax_id BIGINT NULL AFTER exchange_rate,
    ADD COLUMN IF NOT EXISTS tax_code VARCHAR(50) NULL AFTER tax_id,
    ADD COLUMN IF NOT EXISTS tax_name VARCHAR(150) NULL AFTER tax_code,
    ADD COLUMN IF NOT EXISTS tax_rate DECIMAL(10,4) NOT NULL DEFAULT 0 AFTER tax_name,
    ADD COLUMN IF NOT EXISTS tax_calculation_mode VARCHAR(20) NOT NULL DEFAULT 'EXCLUSIVE' AFTER tax_rate;

UPDATE pur_purchase_orders po
JOIN (
    SELECT header_id,
           CASE WHEN MIN(tax_rate) = MAX(tax_rate) THEN MAX(tax_rate) ELSE NULL END AS uniform_tax_rate
    FROM pur_purchase_order_lines
    GROUP BY header_id
) pol ON pol.header_id = po.id
SET po.tax_rate = COALESCE(pol.uniform_tax_rate, 0),
    po.tax_calculation_mode = 'EXCLUSIVE'
WHERE po.tax_rate = 0;
