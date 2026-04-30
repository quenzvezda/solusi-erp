-- Development seeder inventory product categories.

INSERT INTO product_categories (code, name, type, note, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('CAT-DEMO-01', 'Smartphone', 'STOCK', 'Kategori untuk perangkat telepon pintar', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-02', 'Furniture', 'STOCK', 'Kategori untuk perabotan kantor dan rumah', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-03', 'Service', 'SERVICE', 'Kategori untuk jasa dan layanan', 1, 1, NOW(), 1, NOW());
