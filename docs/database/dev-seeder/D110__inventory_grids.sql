-- Development seeder inventory grids.

SET @fac_main_id = (SELECT id FROM inv_facilities WHERE code = 'FAC-DEMO-01');
SET @fac_transit_id = (SELECT id FROM inv_facilities WHERE code = 'FAC-DEMO-02');
SET @fac_sparepart_id = (SELECT id FROM inv_facilities WHERE code = 'FAC-DEMO-03');
SET @fac_staging_id = (SELECT id FROM inv_facilities WHERE code = 'FAC-DEMO-04');

INSERT INTO inv_grids (facility_id, code, name, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
(@fac_main_id, 'GRD-DEMO-A', 'Area A - Elektronik', 'Khusus barang elektronik & gadget', 1, 1, 1, NOW(), 1, NOW()),
(@fac_main_id, 'GRD-DEMO-B', 'Area B - Furnitur', 'Barang berukuran besar', 1, 1, 1, NOW(), 1, NOW()),
(@fac_main_id, 'GRD-DEMO-C', 'Area C - Pendingin', 'Suhu terkontrol 0-5°C', 1, 1, 1, NOW(), 1, NOW()),
(@fac_transit_id, 'GRD-DEMO-T1', 'Transit Lane 1', 'Area staging inbound dan outbound cepat', 1, 1, 1, NOW(), 1, NOW()),
(@fac_transit_id, 'GRD-DEMO-T2', 'Cross Dock Zone', 'Area transfer barang antar kendaraan', 1, 1, 1, NOW(), 1, NOW()),
(@fac_sparepart_id, 'GRD-DEMO-S1', 'Serial Cage', 'Penyimpanan item serialized bernilai tinggi', 1, 1, 1, NOW(), 1, NOW()),
(@fac_sparepart_id, 'GRD-DEMO-S2', 'Technician Bench', 'Area sparepart servis dan testing teknisi', 1, 1, 1, NOW(), 1, NOW()),
(@fac_staging_id, 'GRD-DEMO-R1', 'Return Inspection', 'Area inspeksi barang retur dan RMA', 1, 1, 1, NOW(), 1, NOW()),
(@fac_staging_id, 'GRD-DEMO-R2', 'Project Buffer', 'Buffer stok untuk kebutuhan proyek dan reservasi', 1, 1, 1, NOW(), 1, NOW());
