-- Development seeder inventory facilities.

SET @p_owner = (SELECT id FROM parties WHERE code = 'PRT-INTERNAL-01');
SET @geo_jkt_pusat = 213; -- Kota Adm. Jakarta Pusat
SET @geo_jkt_selatan = 216; -- Kota Adm. Jakarta Selatan
SET @geo_jkt_timur = 217; -- Kota Adm. Jakarta Timur

INSERT INTO inv_facilities (code, name, owner_id, city_id, address_line1, postal_code, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('FAC-DEMO-01', 'Gudang Utama Jakarta Pusat', @p_owner, @geo_jkt_pusat, 'Jl. Medan Merdeka Barat No. 1, Gambir', '10110', 1, 1, 1, NOW(), 1, NOW()),
('FAC-DEMO-02', 'Gudang Transit Jakarta', @p_owner, @geo_jkt_pusat, 'Jl. Gajah Mada No. 18', '10120', 1, 1, 1, NOW(), 1, NOW()),
('FAC-DEMO-03', 'Gudang Sparepart Jakarta Timur', @p_owner, @geo_jkt_timur, 'Kawasan Industri Pulogadung Blok B No. 12', '13920', 1, 1, 1, NOW(), 1, NOW()),
('FAC-DEMO-04', 'Gudang Staging Jakarta Selatan', @p_owner, @geo_jkt_selatan, 'Jl. TB Simatupang No. 88', '12540', 1, 1, 1, NOW(), 1, NOW());
