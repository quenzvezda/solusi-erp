-- Development seeder inventory grids.

SET @fac_main_id = (SELECT id FROM inv_facilities WHERE code = 'FAC-DEMO-01');

INSERT INTO inv_grids (facility_id, code, name, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
(@fac_main_id, 'GRD-DEMO-A', 'Area A - Elektronik', 'Khusus barang elektronik & gadget', 1, 1, 1, NOW(), 1, NOW()),
(@fac_main_id, 'GRD-DEMO-B', 'Area B - Furnitur', 'Barang berukuran besar', 1, 1, 1, NOW(), 1, NOW()),
(@fac_main_id, 'GRD-DEMO-C', 'Area C - Pendingin', 'Suhu terkontrol 0-5°C', 1, 1, 1, NOW(), 1, NOW());
