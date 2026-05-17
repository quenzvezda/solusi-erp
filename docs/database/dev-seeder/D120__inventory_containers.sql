-- Development seeder inventory containers.

SET @fac_main_id = (SELECT id FROM inv_facilities WHERE code = 'FAC-DEMO-01');
SET @grid_a_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-A' AND facility_id = @fac_main_id);
SET @grid_b_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-B' AND facility_id = @fac_main_id);
SET @grid_c_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-C' AND facility_id = @fac_main_id);
SET @grid_t1_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-T1');
SET @grid_t2_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-T2');
SET @grid_s1_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-S1');
SET @grid_s2_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-S2');
SET @grid_r1_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-R1');
SET @grid_r2_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-R2');

INSERT INTO inv_containers (grid_id, code, name, barcode, length, width, height, max_weight, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
(@grid_a_id, 'BIN-DEMO-A01-01', 'Rak Elektronik A-01-01', 'BC-DEMO-A0101', 50.00, 50.00, 50.00, 100.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_a_id, 'BIN-DEMO-A01-02', 'Rak Elektronik A-01-02', 'BC-DEMO-A0102', 50.00, 50.00, 50.00, 100.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_a_id, 'BIN-DEMO-A02-01', 'Rak Elektronik A-02-01', 'BC-DEMO-A0201', 100.00, 100.00, 100.00, 500.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_b_id, 'BIN-DEMO-B01-01', 'Rak Furnitur B-01-01', 'BC-DEMO-B0101', 150.00, 120.00, 180.00, 800.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_c_id, 'BIN-DEMO-C01-01', 'Cold Bin C-01-01', 'BC-DEMO-C0101', 80.00, 80.00, 120.00, 250.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_t1_id, 'BIN-DEMO-T1-01', 'Transit Pallet T1-01', 'BC-DEMO-T101', 120.00, 120.00, 150.00, 1000.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_t2_id, 'BIN-DEMO-T2-01', 'Cross Dock T2-01', 'BC-DEMO-T201', 120.00, 120.00, 150.00, 1000.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_s1_id, 'BIN-DEMO-S1-01', 'Serial Locker S1-01', 'BC-DEMO-S101', 40.00, 40.00, 60.00, 80.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_s2_id, 'BIN-DEMO-S2-01', 'Workbench Bin S2-01', 'BC-DEMO-S201', 60.00, 50.00, 40.00, 120.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_r1_id, 'BIN-DEMO-R1-01', 'Return Rack R1-01', 'BC-DEMO-R101', 100.00, 80.00, 120.00, 300.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_r2_id, 'BIN-DEMO-R2-01', 'Project Buffer R2-01', 'BC-DEMO-R201', 120.00, 100.00, 140.00, 500.00, 1, 1, 1, NOW(), 1, NOW());
