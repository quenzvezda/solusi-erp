-- V3: Inventory Module - Product Category
-- Mandate: AGENTS.md Section 4 & 5

CREATE TABLE product_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type ENUM('STOCK', 'NON_STOCK', 'SERVICE') NOT NULL DEFAULT 'STOCK',
    note TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
);

-- Seed Permissions for Product Category
INSERT INTO permissions (name, description, created_by, created_date) VALUES 
('PRODUCT-CATEGORY_READ', 'Melihat Daftar Kategori Produk', 'SYSTEM', NOW()),
('PRODUCT-CATEGORY_CREATE', 'Menambah Kategori Produk Baru', 'SYSTEM', NOW()),
('PRODUCT-CATEGORY_UPDATE', 'Mengubah Data Kategori Produk', 'SYSTEM', NOW()),
('PRODUCT-CATEGORY_DELETE', 'Menghapus Kategori Produk', 'SYSTEM', NOW());

-- Add New Permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'PRODUCT-CATEGORY_%';
