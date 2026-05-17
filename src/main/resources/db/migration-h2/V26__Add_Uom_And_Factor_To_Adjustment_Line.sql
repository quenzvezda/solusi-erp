-- Add UOM and Conversion Factor to Stock Adjustment Lines for persistence and audit trail
ALTER TABLE inv_stock_adjustment_lines ADD COLUMN uom_id BIGINT;
ALTER TABLE inv_stock_adjustment_lines ADD COLUMN conversion_factor DECIMAL(19, 4);

-- Add foreign key constraint
ALTER TABLE inv_stock_adjustment_lines ADD CONSTRAINT fk_inv_adjustment_line_uom 
    FOREIGN KEY (uom_id) REFERENCES unit_of_measures (id);

-- Update existing data (assume 1.00 factor and product base UOM for existing records if any)
-- This is a safe fallback
UPDATE inv_stock_adjustment_lines SET
    conversion_factor = 1.00,
    uom_id = (SELECT p.uom_id FROM products p WHERE p.id = inv_stock_adjustment_lines.product_id)
WHERE conversion_factor IS NULL;
