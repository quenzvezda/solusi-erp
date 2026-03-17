-- V22: Add Permissions for Warehouse Hierarchy
-- Standard: AGENTS.md Section 6 & docs/spec/search-menu.md

-- 1. Insert Permission Groups for Global Search
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, created_by_user_id, created_date) VALUES
('INV-05', 'Fasilitas', 'Facilities', 'Operasional > Manajemen Inventaris > Fasilitas', 'Operations > Inventory Management > Facilities', '/inventory/facilities', 'ti-building-warehouse', 'Manajemen gedung/gudang', 'Manage warehouse buildings', 1, NOW()),
('INV-06', 'Grid', 'Grids', 'Operasional > Manajemen Inventaris > Grid', 'Operations > Inventory Management > Grids', '/inventory/grids', 'ti-layout-grid', 'Manajemen area/zona gudang', 'Manage warehouse zones', 1, NOW()),
('INV-07', 'Kontainer', 'Containers', 'Operasional > Manajemen Inventaris > Kontainer', 'Operations > Inventory Management > Containers', '/inventory/containers', 'ti-box', 'Manajemen rak/bin gudang', 'Manage bins/racks', 1, NOW());

-- 2. Insert Permissions
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
-- Facility
('FACILITY_READ', 'Melihat daftar fasilitas', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-05')),
('FACILITY_CREATE', 'Menambah fasilitas baru', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-05')),
('FACILITY_UPDATE', 'Mengubah data fasilitas', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-05')),
('FACILITY_DELETE', 'Menghapus fasilitas', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-05')),
-- Grid
('GRID_READ', 'Melihat daftar grid', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-06')),
('GRID_CREATE', 'Menambah grid baru', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-06')),
('GRID_UPDATE', 'Mengubah data grid', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-06')),
('GRID_DELETE', 'Menghapus grid', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-06')),
-- Container
('CONTAINER_READ', 'Melihat daftar kontainer', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-07')),
('CONTAINER_CREATE', 'Menambah kontainer baru', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-07')),
('CONTAINER_UPDATE', 'Mengubah data kontainer', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-07')),
('CONTAINER_DELETE', 'Menghapus kontainer', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-07')),
-- Lookup Party
('LOOKUP_PARTY', 'Mencari dan Memilih Partner (Popup/Autocomplete)', 1, NOW(), NULL);

-- 3. Grant permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND (p.name LIKE 'FACILITY\_%' ESCAPE '\\' 
  OR p.name LIKE 'GRID\_%' ESCAPE '\\' 
  OR p.name LIKE 'CONTAINER\_%' ESCAPE '\\'
  OR p.name = 'LOOKUP_PARTY');
