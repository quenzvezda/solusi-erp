-- DEV SEEDER: Data Dummy untuk Keperluan Development & Testing
-- Lokasi File: docs/database/dev-seeder.sql
-- Deskripsi: Mengisi data awal untuk Party Internal, Fasilitas, Grid, dan Kontainer.

-- ============================================================
-- 1. CLEANUP (Mencegah Duplikasi)
-- ============================================================
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
