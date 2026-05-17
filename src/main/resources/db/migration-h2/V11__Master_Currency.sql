-- V11: Master Module - Currency
-- Mandate: AGENTS.md Section 4 & 5

CREATE TABLE master_currencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    alias VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    note TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- BaseModel fields
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
);

-- Seed Data untuk Mata Uang Internasional
INSERT INTO master_currencies (symbol, alias, name, note, is_active, is_default, created_by, created_date) VALUES 
('$', 'USD', 'US Dollar', 'US Dollar', TRUE, FALSE, 'SYSTEM', NOW()),
('€', 'EUR', 'Euro', 'European Euro', TRUE, FALSE, 'SYSTEM', NOW()),
('£', 'GBP', 'British Pound', 'British Pound Sterling', TRUE, FALSE, 'SYSTEM', NOW()),
('¥', 'JPY', 'Japanese Yen', 'Japanese Yen', TRUE, FALSE, 'SYSTEM', NOW()),
('Rp', 'IDR', 'Indonesian Rupiah', 'Indonesian Rupiah', TRUE, TRUE, 'SYSTEM', NOW()),
('S$', 'SGD', 'Singapore Dollar', 'Singapore Dollar', TRUE, FALSE, 'SYSTEM', NOW()),
('A$', 'AUD', 'Australian Dollar', 'Australian Dollar', TRUE, FALSE, 'SYSTEM', NOW()),
('¥', 'CNY', 'Chinese Yuan', 'Chinese Yuan Renminbi', TRUE, FALSE, 'SYSTEM', NOW());

-- Seed Permissions for Currency
INSERT INTO permissions (name, description, created_by, created_date) VALUES 
('CURRENCY_READ', 'Melihat Daftar Mata Uang', 'SYSTEM', NOW()),
('CURRENCY_CREATE', 'Menambah Mata Uang Baru', 'SYSTEM', NOW()),
('CURRENCY_UPDATE', 'Mengubah Data Mata Uang', 'SYSTEM', NOW()),
('CURRENCY_DELETE', 'Menghapus Mata Uang', 'SYSTEM', NOW());

-- Add New Permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'CURRENCY\_%' ESCAPE '\';
