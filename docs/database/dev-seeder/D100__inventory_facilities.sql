-- Development seeder inventory facilities.

SET @p_owner = (SELECT id FROM parties WHERE code = 'PRT-INTERNAL-01');
SET @geo_jkt_pusat = 213; -- Kota Adm. Jakarta Pusat

INSERT INTO inv_facilities (code, name, owner_id, city_id, address_line1, postal_code, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('FAC-DEMO-01', 'Gudang Utama Jakarta Pusat', @p_owner, @geo_jkt_pusat, 'Jl. Medan Merdeka Barat No. 1, Gambir', '10110', 1, 1, 1, NOW(), 1, NOW()),
('FAC-DEMO-02', 'Gudang Transit Jakarta', @p_owner, @geo_jkt_pusat, 'Jl. Gajah Mada No. 18', '10120', 1, 1, 1, NOW(), 1, NOW());
