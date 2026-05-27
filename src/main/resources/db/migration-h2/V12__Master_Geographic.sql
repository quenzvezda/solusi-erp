-- V12: Master Module - Geographic
-- Mandate: AGENTS.md Section 4, 5 & 6

CREATE TABLE geographics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(50) NOT NULL, -- COUNTRY, STATE_PROVINCE, CITY_MUNICIPALITY
    parent_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- BaseModel fields
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1,

    CONSTRAINT fk_geographic_parent FOREIGN KEY (parent_id) REFERENCES geographics(id)
);

-- 1. Seed Countries (Top Level)
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) VALUES 
('ID', 'Indonesia', 'COUNTRY', NULL, 'SYSTEM', NOW()),
('US', 'United States', 'COUNTRY', NULL, 'SYSTEM', NOW()),
('GB', 'United Kingdom', 'COUNTRY', NULL, 'SYSTEM', NOW()),
('JP', 'Japan', 'COUNTRY', NULL, 'SYSTEM', NOW()),
('SG', 'Singapore', 'COUNTRY', NULL, 'SYSTEM', NOW()),
('AU', 'Australia', 'COUNTRY', NULL, 'SYSTEM', NOW()),
('CN', 'China', 'COUNTRY', NULL, 'SYSTEM', NOW()),
('DE', 'Germany', 'COUNTRY', NULL, 'SYSTEM', NOW());

-- 2. Seed State/Provinces (Parent: Countries)
-- Indonesia
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'ID-JK', 'DKI Jakarta', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'ID-JB', 'Jawa Barat', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'ID-JT', 'Jawa Tengah', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'ID-JI', 'Jawa Timur', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID';

-- USA
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'US-CA', 'California', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'US';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'US-NY', 'New York', 'STATE_PROVINCE', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'US';

-- 3. Seed Cities (Parent: Provinces/Countries)
-- Indonesia Cities
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'ID-CITY-JKT', 'Jakarta Central', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID-JK';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'ID-CITY-BDO', 'Bandung', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID-JB';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'ID-CITY-SUB', 'Surabaya', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'ID-JI';

-- International Cities
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'US-CITY-LA', 'Los Angeles', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'US-CA';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'US-CITY-NYC', 'New York City', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'US-NY';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'GB-CITY-LON', 'London', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'GB';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'JP-CITY-TKO', 'Tokyo', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'JP';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'SG-CITY-SG', 'Singapore City', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'SG';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'AU-CITY-SYD', 'Sydney', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'AU';
INSERT INTO geographics (code, name, type, parent_id, created_by, created_date) 
SELECT 'CN-CITY-BJ', 'Beijing', 'CITY_MUNICIPALITY', id, 'SYSTEM', NOW() FROM geographics WHERE code = 'CN';

-- Seed Permissions for Geographic
INSERT INTO permissions (name, description, created_by, created_date) VALUES 
('GEOGRAPHIC_READ', 'Melihat Daftar Wilayah Geografis', 'SYSTEM', NOW()),
('GEOGRAPHIC_CREATE', 'Menambah Wilayah Geografis Baru', 'SYSTEM', NOW()),
('GEOGRAPHIC_UPDATE', 'Mengubah Data Wilayah Geografis', 'SYSTEM', NOW()),
('GEOGRAPHIC_DELETE', 'Menghapus Wilayah Geografis', 'SYSTEM', NOW());

-- Add New Permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'GEOGRAPHIC\_%' ESCAPE '\';
