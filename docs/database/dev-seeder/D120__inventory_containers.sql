-- Development seeder inventory containers.

SET @fac_main_id = (SELECT id FROM inv_facilities WHERE code = 'FAC-DEMO-01');
SET @grid_a_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-A' AND facility_id = @fac_main_id);

INSERT INTO inv_containers (grid_id, code, name, barcode, length, width, height, max_weight, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
(@grid_a_id, 'BIN-DEMO-A01-01', 'Rak Elektronik A-01-01', 'BC-DEMO-A0101', 50.00, 50.00, 50.00, 100.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_a_id, 'BIN-DEMO-A01-02', 'Rak Elektronik A-01-02', 'BC-DEMO-A0102', 50.00, 50.00, 50.00, 100.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_a_id, 'BIN-DEMO-A02-01', 'Rak Elektronik A-02-01', 'BC-DEMO-A0201', 100.00, 100.00, 100.00, 500.00, 1, 1, 1, NOW(), 1, NOW());
