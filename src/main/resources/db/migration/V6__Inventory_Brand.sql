-- V6: Inventory Module - Brand
-- Mandate: AGENTS.md Section 4 & 5

CREATE TABLE brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    note TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- Initial Seed for Brand Sequence
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by, updated_date)
VALUES ('BRAND', 'BRD-{seq}', 4, 'NEVER', 'SYSTEM', NOW());

-- Seed Permissions for Brand
INSERT INTO permissions (name, description, created_by, created_date) VALUES 
('BRAND_READ', 'Melihat Daftar Brand', 'SYSTEM', NOW()),
('BRAND_CREATE', 'Menambah Brand Baru', 'SYSTEM', NOW()),
('BRAND_UPDATE', 'Mengubah Data Brand', 'SYSTEM', NOW()),
('BRAND_DELETE', 'Menghapus Brand', 'SYSTEM', NOW());

-- Add New Permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'BRAND_%';
