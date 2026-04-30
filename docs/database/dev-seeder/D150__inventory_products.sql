-- Development seeder inventory products.

SET @cat_smartphone_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-01');
SET @cat_furniture_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-02');
SET @cat_service_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-03');
SET @cat_accessories_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-04');
SET @cat_networking_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-05');
SET @cat_office_supplies_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-06');
SET @cat_spare_parts_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-07');
SET @brd_samsung_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-01');
SET @brd_ikea_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-02');
SET @brd_generic_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-03');
SET @brd_apple_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-04');
SET @brd_logitech_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-05');
SET @brd_epson_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-06');
SET @brd_schneider_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-07');
SET @uom_pcs_id = (SELECT id FROM unit_of_measures WHERE code = 'PCS');
SET @uom_hr_id = (SELECT id FROM unit_of_measures WHERE code = 'HR');
SET @uom_box_id = (SELECT id FROM unit_of_measures WHERE code = 'BOX');
SET @uom_roll_id = (SELECT id FROM unit_of_measures WHERE code = 'ROLL');
SET @uom_set_id = (SELECT id FROM unit_of_measures WHERE code = 'SET');
SET @uom_btl_id = (SELECT id FROM unit_of_measures WHERE code = 'BTL');

INSERT INTO products (
    code,
    name,
    barcode,
    note,
    category_id,
    uom_id,
    brand_id,
    hscode,
    is_active,
    is_serialized,
    min_stock,
    max_stock,
    version,
    created_by_user_id,
    created_date,
    updated_by_user_id,
    updated_date
)
VALUES
('PRD-DEMO-0001', 'Samsung Galaxy S24 Ultra', '8806095300001', 'Smartphone flagship dengan pencatatan serial number.', @cat_smartphone_id, @uom_pcs_id, @brd_samsung_id, '85171300', 1, 1, 5, 25, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0002', 'IKEA Billy Bookcase White', '20135268', 'Rak buku untuk kebutuhan kantor dan proyek interior.', @cat_furniture_id, @uom_pcs_id, @brd_ikea_id, '94036090', 1, 0, 2, 10, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0003', 'Laptop Cleaning Service', NULL, 'Layanan pembersihan laptop onsite per jam kerja.', @cat_service_id, @uom_hr_id, @brd_generic_id, NULL, 1, 0, 0, 0, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0004', 'iPhone 15 128GB', '194253000128', 'Produk serialized untuk simulasi inbound dan sales premium.', @cat_smartphone_id, @uom_pcs_id, @brd_apple_id, '85171300', 1, 1, 3, 15, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0005', 'Logitech MX Master 3S', '097855180001', 'Peripheral high-end untuk kebutuhan office dan engineering.', @cat_accessories_id, @uom_pcs_id, @brd_logitech_id, '84716070', 1, 0, 10, 40, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0006', 'LAN Cable Box 305m', 'LAN305MBOX01', 'Kabel jaringan dalam kemasan box untuk instalasi proyek.', @cat_networking_id, @uom_box_id, @brd_schneider_id, '85444929', 1, 0, 4, 20, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0007', 'Epson Label Roll 100x150', 'LBL100150RL', 'Consumable label printer dengan base UoM roll.', @cat_office_supplies_id, @uom_roll_id, @brd_epson_id, '48219090', 1, 0, 12, 60, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0008', 'Printer Ink Set CMYK', 'INKSETCMYK01', 'Satu set tinta printer untuk kebutuhan operasional.', @cat_spare_parts_id, @uom_set_id, @brd_epson_id, '32159090', 1, 0, 6, 24, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0009', 'Drinking Water 600ml', '8996001600009', 'Persediaan minuman botol untuk pantry kantor.', @cat_office_supplies_id, @uom_btl_id, @brd_generic_id, '22011010', 1, 0, 48, 240, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0010', 'Access Door Controller', 'ADCCTRL0001', 'Perangkat access control dengan pelacakan serial number.', @cat_networking_id, @uom_pcs_id, @brd_schneider_id, '85371019', 1, 1, 2, 8, 1, 1, NOW(), 1, NOW());
