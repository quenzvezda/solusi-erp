-- Development seeder inventory products.

SET @cat_smartphone_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-01');
SET @cat_furniture_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-02');
SET @cat_service_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-03');
SET @brd_samsung_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-01');
SET @brd_ikea_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-02');
SET @brd_generic_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-03');
SET @uom_pcs_id = (SELECT id FROM unit_of_measures WHERE code = 'PCS');

INSERT INTO products (code, name, barcode, category_id, uom_id, brand_id, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('PRD-DEMO-0001', 'Samsung Galaxy S24 Ultra', '8806095300001', @cat_smartphone_id, @uom_pcs_id, @brd_samsung_id, 1, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0002', 'IKEA Billy Bookcase White', '20135268', @cat_furniture_id, @uom_pcs_id, @brd_ikea_id, 1, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0003', 'Laptop Cleaning Service', NULL, @cat_service_id, @uom_pcs_id, @brd_generic_id, 1, 1, 1, NOW(), 1, NOW());
