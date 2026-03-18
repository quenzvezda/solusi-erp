-- V30: Add Facility to Adjustment and Lookup Permissions

-- 1. Update Stock Adjustment Tables
ALTER TABLE inv_stock_adjustments
    ADD COLUMN facility_id BIGINT AFTER note,
    ADD CONSTRAINT fk_stock_adj_facility FOREIGN KEY (facility_id) REFERENCES inv_facilities(id);

ALTER TABLE inv_stock_adjustment_lines
    ADD COLUMN grid_id BIGINT AFTER product_id,
    ADD CONSTRAINT fk_stock_adj_line_grid FOREIGN KEY (grid_id) REFERENCES inv_grids(id);

-- 2. Create LOOKUP_INVENTORY permission
INSERT INTO permissions (name, description, created_by_user_id, created_date) VALUES
('LOOKUP_INVENTORY', 'Akses untuk mencari data referensi inventaris (Produk, Gudang, Bin)', 1, NOW());

-- 3. Grant to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' AND p.name = 'LOOKUP_INVENTORY';
