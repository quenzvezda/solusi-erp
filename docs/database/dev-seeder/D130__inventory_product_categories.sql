-- Development seeder inventory product categories.

INSERT INTO product_categories (code, name, type, note, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('CAT-DEMO-01', 'Smartphone', 'STOCK', 'Kategori untuk perangkat telepon pintar', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-02', 'Furniture', 'STOCK', 'Kategori untuk perabotan kantor dan rumah', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-03', 'Service', 'SERVICE', 'Kategori untuk jasa dan layanan', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-04', 'Accessories', 'STOCK', 'Aksesori perangkat dan perlengkapan pendukung', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-05', 'Networking', 'STOCK', 'Perangkat jaringan dan konektivitas', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-06', 'Office Supplies', 'STOCK', 'Barang operasional kantor dan kebutuhan harian', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-07', 'Spare Parts', 'STOCK', 'Komponen pengganti dan item servis', 1, 1, NOW(), 1, NOW());
