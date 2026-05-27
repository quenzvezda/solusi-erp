-- V19: Add Localized Permission Groups for Global Search Menu
-- Standard: AGENTS.md Section 5

CREATE TABLE permission_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name_id VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    breadcrumb_id VARCHAR(255) NOT NULL,
    breadcrumb_en VARCHAR(255) NOT NULL,
    url_path VARCHAR(255) NOT NULL,
    description_id VARCHAR(255),
    description_en VARCHAR(255),
    created_by_user_id BIGINT NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by_user_id BIGINT,
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_permission_groups_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_permission_groups_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
);

ALTER TABLE permissions
    ADD COLUMN permission_group_id BIGINT NULL;

ALTER TABLE permissions
    ADD CONSTRAINT fk_permissions_permission_group FOREIGN KEY (permission_group_id) REFERENCES permission_groups(id);

-- Insert Localized Menu Groups
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, description_id, description_en, created_by_user_id, created_date) VALUES
('SEC-01', 'Pengguna', 'Users', 'Keamanan > Pengguna', 'Security > Users', '/security/users', 'Manajemen pengguna sistem', 'Manage system users', 1, NOW()),
('SEC-02', 'Peran', 'Roles', 'Keamanan > Peran', 'Security > Roles', '/security/roles', 'Manajemen peran & hak akses', 'Manage roles & access rights', 1, NOW()),
('SEC-03', 'Otoritas', 'Permissions', 'Keamanan > Otoritas', 'Security > Permissions', '/security/permissions', 'Daftar otoritas sistem', 'System access authorities', 1, NOW()),
('SEC-04', 'Grup Menu', 'Menu Groups', 'Keamanan > Grup Menu', 'Security > Menu Groups', '/security/menu-groups', 'Manajemen identitas menu pencarian global', 'Manage search menu identities', 1, NOW()),
('INV-01', 'Produk', 'Products', 'Inventori > Produk', 'Inventory > Products', '/inventory/products', 'Manajemen master produk', 'Manage product master data', 1, NOW()),
('INV-02', 'Kategori Produk', 'Product Categories', 'Inventori > Kategori Produk', 'Inventory > Product Categories', '/inventory/product-categories', 'Manajemen kategori produk', 'Manage product categories', 1, NOW()),
('INV-03', 'Brand', 'Brands', 'Inventori > Brand', 'Inventory > Brands', '/inventory/brands', 'Manajemen brand produk', 'Manage product brands', 1, NOW()),
('INV-04', 'Satuan Ukur', 'Units of Measure', 'Inventori > Satuan Ukur', 'Inventory > Units of Measure', '/inventory/unit-of-measures', 'Manajemen satuan (UoM)', 'Manage units of measure (UoM)', 1, NOW()),
('MST-01', 'Business Partner', 'Business Partners', 'Master > Business Partner', 'Master > Business Partners', '/master/parties', 'Manajemen pelanggan & pemasok', 'Manage customers & suppliers', 1, NOW()),
('MST-02', 'Data Geografis', 'Geographic Data', 'Master > Geografis', 'Master > Geographic', '/master/geographics', 'Manajemen data wilayah', 'Manage geographic data', 1, NOW()),
('MST-03', 'Rekening Bank', 'Bank Accounts', 'Master > Rekening Bank', 'Master > Bank Accounts', '/master/bank-accounts', 'Manajemen akun bank perusahaan', 'Manage company bank accounts', 1, NOW()),
('MST-04', 'Pajak', 'Taxes', 'Master > Pajak', 'Master > Taxes', '/master/tax', 'Manajemen data pajak', 'Manage tax data', 1, NOW()),
('MST-05', 'Mata Uang', 'Currencies', 'Master > Mata Uang', 'Master > Currencies', '/master/currency', 'Manajemen mata uang aktif', 'Manage active currencies', 1, NOW()),
('MST-06', 'Tipe Peran Partner', 'Partner Role Types', 'Master > Tipe Peran Partner', 'Master > Partner Role Types', '/master/party-role-types', 'Manajemen tipe peran partner', 'Manage partner role types', 1, NOW());

-- Add granular permissions for MENU-GROUP itself
INSERT INTO permissions (name, description, created_by_user_id, created_date) VALUES
('MENU-GROUP_READ', 'Melihat daftar grup menu', 1, NOW()),
('MENU-GROUP_CREATE', 'Menambah grup menu baru', 1, NOW()),
('MENU-GROUP_UPDATE', 'Mengubah data grup menu', 1, NOW()),
('MENU-GROUP_DELETE', 'Menghapus grup menu', 1, NOW());

-- Grant new permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' AND p.name LIKE 'MENU-GROUP\_%' ESCAPE '\';

-- Link existing permissions to groups
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'SEC-01') WHERE name LIKE 'USERS\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'SEC-02') WHERE name LIKE 'ROLES\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'SEC-03') WHERE name LIKE 'PERMISSIONS\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'SEC-04') WHERE name LIKE 'MENU-GROUP\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'INV-01') WHERE name LIKE 'PRODUCT\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'INV-02') WHERE name LIKE 'PRODUCT-CATEGORY\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'INV-03') WHERE name LIKE 'BRAND\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'INV-04') WHERE name LIKE 'UNIT-OF-MEASURE\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'MST-01') WHERE name LIKE 'PARTY\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'MST-02') WHERE name LIKE 'GEOGRAPHIC\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'MST-03') WHERE name LIKE 'BANK-ACCOUNT\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'MST-04') WHERE name LIKE 'TAX\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'MST-05') WHERE name LIKE 'CURRENCY\_%' ESCAPE '\';
UPDATE permissions SET permission_group_id = (SELECT id FROM permission_groups WHERE code = 'MST-06') WHERE name LIKE 'PARTY-ROLE-TYPE\_%' ESCAPE '\';
