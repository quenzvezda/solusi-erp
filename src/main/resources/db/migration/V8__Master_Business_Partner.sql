-- V8: Master Module - Business Partner (Party)
-- Mandate: AGENTS.md Section 4 & 5

-- 1. Party Role Types
CREATE TABLE party_role_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- 2. Party Identification Types
CREATE TABLE party_id_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- 3. Party Base Table
CREATE TABLE parties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type ENUM('PERSON', 'ORGANIZATION') NOT NULL,
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email VARCHAR(100),
    phone VARCHAR(50),
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- 4. Party Roles (Many-to-Many Link)
CREATE TABLE party_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    party_id BIGINT NOT NULL,
    role_type_id BIGINT NOT NULL,
    CONSTRAINT fk_party_role_party FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE,
    CONSTRAINT fk_party_role_type FOREIGN KEY (role_type_id) REFERENCES party_role_types(id)
) ENGINE=InnoDB;

-- 5. Party Identifications
CREATE TABLE party_identifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    party_id BIGINT NOT NULL,
    id_type_id BIGINT NOT NULL,
    id_number VARCHAR(100) NOT NULL,
    issued_date DATE,
    expiry_date DATE,
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_party_id_party FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE,
    CONSTRAINT fk_party_id_type FOREIGN KEY (id_type_id) REFERENCES party_id_types(id)
) ENGINE=InnoDB;

-- 6. Party Addresses
CREATE TABLE party_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    party_id BIGINT NOT NULL,
    type ENUM('MAIN', 'BILLING', 'SHIPPING', 'OTHER') NOT NULL DEFAULT 'MAIN',
    address_line1 TEXT NOT NULL,
    city VARCHAR(100),
    province VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'Indonesia',
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_party_address_party FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Seed Standard Role Types
INSERT INTO party_role_types (code, name, created_by, created_date) VALUES 
('INTERNAL', 'Internal / Branch', 'SYSTEM', NOW()),
('CUSTOMER', 'Pelanggan (Customer)', 'SYSTEM', NOW()),
('SUPPLIER', 'Pemasok (Supplier)', 'SYSTEM', NOW()),
('EMPLOYEE', 'Karyawan (Employee)', 'SYSTEM', NOW()),
('COURIER', 'Kurir (Courier)', 'SYSTEM', NOW()),
('WAREHOUSE_OPERATOR', 'Petugas Gudang', 'SYSTEM', NOW());

-- Seed Standard ID Types
INSERT INTO party_id_types (code, name, created_by, created_date) VALUES 
('KTP', 'Kartu Tanda Penduduk', 'SYSTEM', NOW()),
('NPWP', 'Nomor Pokok Wajib Pajak', 'SYSTEM', NOW()),
('NIB', 'Nomor Induk Berusaha', 'SYSTEM', NOW()),
('PASSPORT', 'Paspor', 'SYSTEM', NOW());

-- Initial Seed for Party Sequence
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by, updated_date)
VALUES ('PARTY', 'BP-{seq}', 5, 'NEVER', 'SYSTEM', NOW());

-- Seed Permissions for Party
INSERT INTO permissions (name, description, created_by, created_date) VALUES 
('PARTY_READ', 'Melihat Daftar Business Partner', 'SYSTEM', NOW()),
('PARTY_CREATE', 'Menambah Business Partner Baru', 'SYSTEM', NOW()),
('PARTY_UPDATE', 'Mengubah Data Business Partner', 'SYSTEM', NOW()),
('PARTY_DELETE', 'Menghapus Business Partner', 'SYSTEM', NOW());

-- Add New Permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'PARTY\_%' ESCAPE '\\';
