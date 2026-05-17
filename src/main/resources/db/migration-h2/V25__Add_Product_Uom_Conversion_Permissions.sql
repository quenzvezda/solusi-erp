-- V25: Add Product UoM Conversion Permissions and Menu Group
-- Standard: AGENTS.md Section 6 & docs/spec/search-menu.md

-- 1. Insert Permission Group for Global Search
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, created_by_user_id, created_date) VALUES
('INV-11', 'Konversi UoM Produk', 'Product UoM Conversions', 'Operasional > Setup Inventaris > Konversi UoM', 'Operations > Inventory Setup > UoM Conversions', '/inventory/uom-conversions', 'ti-arrows-left-right', 'Manajemen faktor konversi satuan produk', 'Manage product unit conversion factors', 1, NOW());

-- 2. Insert Permissions
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('UOM-CONVERSION_READ', 'Melihat daftar konversi UoM', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('UOM-CONVERSION_CREATE', 'Menambah konversi UoM baru', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('UOM-CONVERSION_UPDATE', 'Mengubah data konversi UoM', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('UOM-CONVERSION_DELETE', 'Menghapus konversi UoM', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11'));

-- 3. Grant permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'UOM-CONVERSION\_%' ESCAPE '\';
