-- Development seeder inventory brands.

INSERT INTO brands (code, name, note, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('BRD-DEMO-01', 'Samsung', 'Brand elektronik Korea Selatan', 1, 1, NOW(), 1, NOW()),
('BRD-DEMO-02', 'IKEA', 'Brand furnitur Swedia', 1, 1, NOW(), 1, NOW()),
('BRD-DEMO-03', 'Generic', 'Brand umum tanpa merk spesifik', 1, 1, NOW(), 1, NOW());
