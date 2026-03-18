-- V28: Refactor Inventory Menu and Add New Groups
-- Standard: AGENTS.md Section 6 & docs/spec/search-menu.md

-- 1. Update Existing Breadcrumbs to 'Inventory Setup'
UPDATE permission_groups 
SET breadcrumb_id = REPLACE(breadcrumb_id, 'Manajemen Inventaris', 'Setup Inventaris'),
    breadcrumb_en = REPLACE(breadcrumb_en, 'Inventory Management', 'Inventory Setup')
WHERE code IN ('INV-01', 'INV-02', 'INV-03', 'INV-04', 'INV-05', 'INV-06', 'INV-07');

-- 2. Insert New Permission Groups for Transactions and Reports
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, created_by_user_id, created_date) VALUES
('INV-08', 'Penyesuaian Stok', 'Stock Adjustment', 'Operasional > Transaksi Inventaris > Penyesuaian Stok', 'Operations > Inventory Transactions > Stock Adjustment', '/inventory/adjustments', 'ti-adjustments', 'Penyesuaian stok manual', 'Manual stock adjustment', 1, NOW()),
('INV-09', 'Kartu Stok', 'Stock Card', 'Operasional > Laporan Inventaris > Kartu Stok', 'Operations > Inventory Reports > Stock Card', '/inventory/reports/stock-card', 'ti-address-book', 'Laporan histori mutasi barang', 'Stock movement history report', 1, NOW()),
('INV-10', 'Stok On-Hand', 'On-Hand Quantity', 'Operasional > Laporan Inventaris > Stok On-Hand', 'Operations > Inventory Reports > On-Hand Quantity', '/inventory/reports/on-hand', 'ti-building-warehouse', 'Laporan saldo stok saat ini', 'Current stock balance report', 1, NOW());

-- 3. Create Permissions for Stock Adjustment
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('STOCK-ADJUSTMENT_READ', 'Melihat daftar penyesuaian stok', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-ADJUSTMENT_CREATE', 'Menambah penyesuaian stok baru', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-ADJUSTMENT_UPDATE', 'Mengubah data penyesuaian stok', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-ADJUSTMENT_DELETE', 'Menghapus penyesuaian stok', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-ADJUSTMENT_PROCESS', 'Memproses penyesuaian stok ke inventaris', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-08')),
('STOCK-CARD_READ', 'Melihat laporan kartu stok', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-09')),
('ON-HAND_READ', 'Melihat laporan stok on-hand', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-10'));

-- 4. Grant permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND (p.name LIKE 'STOCK-ADJUSTMENT\_%' ESCAPE '\\' 
  OR p.name = 'STOCK-CARD_READ'
  OR p.name = 'ON-HAND_READ');
