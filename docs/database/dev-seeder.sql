-- DEV SEEDER: Data Dummy untuk Keperluan Development & Testing
-- Lokasi File: docs/database/dev-seeder.sql
-- Deskripsi: Mengisi data awal untuk Party Internal, Fasilitas, Grid, dan Kontainer.

-- ============================================================
-- 1. CLEANUP (Mencegah Duplikasi)
-- ============================================================
DELETE FROM products WHERE code LIKE 'PRD-DEMO-%';
DELETE FROM brands WHERE code LIKE 'BRD-DEMO-%';
DELETE FROM product_categories WHERE code LIKE 'CAT-DEMO-%';
DELETE FROM inv_containers WHERE code LIKE 'BIN-DEMO-%';
DELETE FROM inv_grids WHERE code LIKE 'GRD-DEMO-%';
DELETE FROM inv_facilities WHERE code LIKE 'FAC-DEMO-%';
DELETE FROM party_roles WHERE party_id IN (SELECT id FROM parties WHERE code = 'PRT-INTERNAL-01');
DELETE FROM parties WHERE code = 'PRT-INTERNAL-01';

-- ============================================================
-- 2. SEED PARTY (Internal Owner)
-- ============================================================
INSERT INTO parties (code, salutation, name, type, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES ('PRT-INTERNAL-01', 'PT.', 'Solusi Program', 'ORGANIZATION', 1, 1, 1, NOW(), 1, NOW());

SET @owner_id = (SELECT id FROM parties WHERE code = 'PRT-INTERNAL-01');

-- Link to 'Internal' Role Type (Assumption: 'INTERNAL' role type exists in party_role_types)
INSERT INTO party_roles (party_id, role_type_id)
SELECT @owner_id, id FROM party_role_types WHERE code = 'INTERNAL' LIMIT 1;

-- ============================================================
-- 3. SEED FACILITY
-- ============================================================
INSERT INTO inv_facilities (code, name, owner_id, city_id, address_line1, postal_code, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES 
('FAC-DEMO-01', 'Gudang Utama Jakarta Pusat', @owner_id, 213, 'Jl. Medan Merdeka Barat No. 1, Gambir', '10110', 1, 1, 1, NOW(), 1, NOW()),
('FAC-DEMO-02', 'Gudang Transit Jakarta', @owner_id, 213, 'Jl. Gajah Mada No. 18', '10120', 1, 1, 1, NOW(), 1, NOW());

SET @fac_main_id = (SELECT id FROM inv_facilities WHERE code = 'FAC-DEMO-01');

-- ============================================================
-- 4. SEED GRID (Zones)
-- ============================================================
INSERT INTO inv_grids (facility_id, code, name, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES 
(@fac_main_id, 'GRD-DEMO-A', 'Area A - Elektronik', 'Khusus barang elektronik & gadget', 1, 1, 1, NOW(), 1, NOW()),
(@fac_main_id, 'GRD-DEMO-B', 'Area B - Furnitur', 'Barang berukuran besar', 1, 1, 1, NOW(), 1, NOW()),
(@fac_main_id, 'GRD-DEMO-C', 'Area C - Pendingin', 'Suhu terkontrol 0-5°C', 1, 1, 1, NOW(), 1, NOW());

SET @grid_a_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-A' AND facility_id = @fac_main_id);

-- ============================================================
-- 5. SEED CONTAINER (Bins/Racks)
-- ============================================================
INSERT INTO inv_containers (grid_id, code, name, barcode, length, width, height, max_weight, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES 
(@grid_a_id, 'BIN-DEMO-A01-01', 'Rak Elektronik A-01-01', 'BC-DEMO-A0101', 50.00, 50.00, 50.00, 100.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_a_id, 'BIN-DEMO-A01-02', 'Rak Elektronik A-01-02', 'BC-DEMO-A0102', 50.00, 50.00, 50.00, 100.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_a_id, 'BIN-DEMO-A02-01', 'Rak Elektronik A-02-01', 'BC-DEMO-A0201', 100.00, 100.00, 100.00, 500.00, 1, 1, 1, NOW(), 1, NOW());

-- ============================================================
-- 6. SEED PRODUCT CATEGORY
-- ============================================================
INSERT INTO product_categories (code, name, type, note, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES 
('CAT-DEMO-01', 'Smartphone', 'STOCK', 'Kategori untuk perangkat telepon pintar', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-02', 'Furniture', 'STOCK', 'Kategori untuk perabotan kantor dan rumah', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-03', 'Service', 'SERVICE', 'Kategori untuk jasa dan layanan', 1, 1, NOW(), 1, NOW());

SET @cat_smartphone_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-01');
SET @cat_furniture_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-02');
SET @cat_service_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-03');

-- ============================================================
-- 7. SEED BRAND
-- ============================================================
INSERT INTO brands (code, name, note, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES 
('BRD-DEMO-01', 'Samsung', 'Brand elektronik Korea Selatan', 1, 1, NOW(), 1, NOW()),
('BRD-DEMO-02', 'IKEA', 'Brand furnitur Swedia', 1, 1, NOW(), 1, NOW()),
('BRD-DEMO-03', 'Generic', 'Brand umum tanpa merk spesifik', 1, 1, NOW(), 1, NOW());

SET @brd_samsung_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-01');
SET @brd_ikea_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-02');
SET @brd_generic_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-03');

-- ============================================================
-- 8. SEED PRODUCT
-- ============================================================
-- Note: UoM IDs are assumed to be available from V5 migration (PCS, BOX, UNIT, etc)
SET @uom_pcs_id = (SELECT id FROM unit_of_measures WHERE code = 'PCS');
SET @uom_unit_id = (SELECT id FROM unit_of_measures WHERE code = 'SET'); -- Fallback to SET if UNIT is actually 'UNIT' type but code is 'PCS'/'BOX'
-- Let's re-verify UoM codes from V5: PCS, BOX, DOZ, ROLL, PACK, SET, BTL are under type 'UNIT'.

INSERT INTO products (code, name, barcode, category_id, uom_id, brand_id, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES 
('PRD-DEMO-0001', 'Samsung Galaxy S24 Ultra', '8806095300001', @cat_smartphone_id, @uom_pcs_id, @brd_samsung_id, 1, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0002', 'IKEA Billy Bookcase White', '20135268', @cat_furniture_id, @uom_pcs_id, @brd_ikea_id, 1, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0003', 'Laptop Cleaning Service', NULL, @cat_service_id, @uom_pcs_id, @brd_generic_id, 1, 1, 1, NOW(), 1, NOW());

