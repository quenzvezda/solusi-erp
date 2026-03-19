-- V24: Inventory Stock Adjustment, Menu, and Permissions (Squashed V28-V31)

-- 1. Refactor Inventory Menu and Add New Groups

-- Update Existing Breadcrumbs to 'Inventory Setup'
UPDATE permission_groups 
SET breadcrumb_id = REPLACE(breadcrumb_id, 'Manajemen Inventaris', 'Setup Inventaris'),
    breadcrumb_en = REPLACE(breadcrumb_en, 'Inventory Management', 'Inventory Setup')
WHERE code IN ('INV-01', 'INV-02', 'INV-03', 'INV-04', 'INV-05', 'INV-06', 'INV-07');

-- Insert New Permission Groups for Transactions and Reports
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, created_by_user_id, created_date) VALUES
('INV-08', 'Penyesuaian Stok', 'Stock Adjustment', 'Operasional > Transaksi Inventaris > Penyesuaian Stok', 'Operations > Inventory Transactions > Stock Adjustment', '/inventory/adjustments', 'ti-adjustments', 'Penyesuaian stok manual', 'Manual stock adjustment', 1, NOW()),
('INV-09', 'Kartu Stok', 'Stock Card', 'Operasional > Laporan Inventaris > Kartu Stok', 'Operations > Inventory Reports > Stock Card', '/inventory/reports/stock-card', 'ti-address-book', 'Laporan histori mutasi barang', 'Stock movement history report', 1, NOW()),
('INV-10', 'Stok On-Hand', 'On-Hand Quantity', 'Operasional > Laporan Inventaris > Stok On-Hand', 'Operations > Inventory Reports > On-Hand Quantity', '/inventory/reports/on-hand', 'ti-building-warehouse', 'Laporan saldo stok saat ini', 'Current stock balance report', 1, NOW());

-- Create Permissions for Stock Adjustment and Lookup
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('STOCK-ADJUSTMENT_READ', 'Melihat daftar penyesuaian stok', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-ADJUSTMENT_CREATE', 'Menambah penyesuaian stok baru', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-ADJUSTMENT_UPDATE', 'Mengubah data penyesuaian stok', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-ADJUSTMENT_DELETE', 'Menghapus penyesuaian stok', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-ADJUSTMENT_PROCESS', 'Memproses penyesuaian stok ke inventaris', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-CARD_READ', 'Melihat laporan kartu stok', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-09')),
('ON-HAND_READ', 'Melihat laporan stok on-hand', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-10'));

-- Permission for Lookup without a specific group
INSERT INTO permissions (name, description, created_by_user_id, created_date) VALUES
('LOOKUP_INVENTORY', 'Akses untuk mencari data referensi inventaris (Produk, Gudang, Bin)', 1, NOW());

-- Grant permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND (p.name LIKE 'STOCK-ADJUSTMENT\_%' ESCAPE '\\' 
  OR p.name = 'STOCK-CARD_READ'
  OR p.name = 'ON-HAND_READ'
  OR p.name = 'LOOKUP_INVENTORY');

-- 2. Stock Adjustment Header Table
CREATE TABLE inv_stock_adjustments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    transaction_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    note TEXT,
    facility_id BIGINT,
    
    -- CurrencyAmount (Embeddable)
    currency_id BIGINT,
    total_exchange_rate DECIMAL(19,6),
    total_amount_original DECIMAL(19,4),
    total_amount_local DECIMAL(19,4),
    
    -- Audit Columns
    version INT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_adj_facility FOREIGN KEY (facility_id) REFERENCES inv_facilities(id),
    CONSTRAINT fk_stock_adj_currency FOREIGN KEY (currency_id) REFERENCES master_currencies(id),
    CONSTRAINT fk_stock_adj_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_stock_adj_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Stock Adjustment Line Table
CREATE TABLE inv_stock_adjustment_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    header_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    grid_id BIGINT,
    container_id BIGINT NOT NULL,
    quantity DECIMAL(19,4) NOT NULL,
    unit_cost DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    serial_number VARCHAR(100),
    
    -- Audit Columns
    version INT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_adj_line_header FOREIGN KEY (header_id) REFERENCES inv_stock_adjustments(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_adj_line_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_stock_adj_line_grid FOREIGN KEY (grid_id) REFERENCES inv_grids(id),
    CONSTRAINT fk_stock_adj_line_container FOREIGN KEY (container_id) REFERENCES inv_containers(id),
    CONSTRAINT fk_stock_adj_line_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_stock_adj_line_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Register Sequence for Stock Adjustment
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date) VALUES
('STOCK_ADJUSTMENT', 'ADJ-{date:yyMM}-{seq}', 5, 'MONTHLY', 1, NOW());
