-- V10: Master Module - Tax
-- Mandate: AGENTS.md Section 4 & 5

CREATE TABLE taxes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    rate DECIMAL(10,4) NOT NULL,
    note TEXT,
    is_subtract BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- BaseModel fields
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- Seed Permissions for Tax
INSERT INTO permissions (name, description, created_by, created_date) VALUES 
('TAX_READ', 'Melihat Daftar Pajak (Tax)', 'SYSTEM', NOW()),
('TAX_CREATE', 'Menambah Pajak Baru', 'SYSTEM', NOW()),
('TAX_UPDATE', 'Mengubah Data Pajak', 'SYSTEM', NOW()),
('TAX_DELETE', 'Menghapus Pajak', 'SYSTEM', NOW());

-- Add New Permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'TAX\_%' ESCAPE '\\';
