-- V31: Add missing LOOKUP_* permissions for inventory features and grant to ROLE_ADMIN

INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('LOOKUP_BRAND',           'Mencari dan Memilih Brand (Autocomplete)',            1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-03')),
('LOOKUP_PRODUCT-CATEGORY','Mencari dan Memilih Kategori Produk (Autocomplete)',  1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-02')),
('LOOKUP_FACILITY',        'Mencari dan Memilih Fasilitas (Autocomplete)',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-05')),
('LOOKUP_GRID',            'Mencari dan Memilih Grid (Autocomplete)',              1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-06')),
('LOOKUP_CONTAINER',       'Mencari dan Memilih Kontainer (Autocomplete)',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-07'));

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('LOOKUP_BRAND', 'LOOKUP_PRODUCT-CATEGORY', 'LOOKUP_FACILITY', 'LOOKUP_GRID', 'LOOKUP_CONTAINER');
