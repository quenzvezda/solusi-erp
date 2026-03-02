-- V5: Inventory Module - Unit of Measure
-- Mandate: AGENTS.md Section 4 & 5

CREATE TABLE unit_of_measures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type ENUM('WEIGHT', 'LENGTH', 'UNIT', 'VOLUME', 'TIME', 'AREA') NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- Seed Permissions for Unit of Measure
INSERT INTO permissions (name, description, created_by, created_date) VALUES 
('UNIT-OF-MEASURE_READ', 'Melihat Daftar Satuan Ukur (UoM)', 'SYSTEM', NOW()),
('UNIT-OF-MEASURE_CREATE', 'Menambah Satuan Ukur Baru', 'SYSTEM', NOW()),
('UNIT-OF-MEASURE_UPDATE', 'Mengubah Data Satuan Ukur', 'SYSTEM', NOW()),
('UNIT-OF-MEASURE_DELETE', 'Menghapus Satuan Ukur', 'SYSTEM', NOW());

-- Add New Permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'UNIT-OF-MEASURE_%';

-- Seed Data for Unit of Measures
INSERT INTO unit_of_measures (code, name, type, created_by, created_date) VALUES 
-- WEIGHT
('KG', 'Kilogram', 'WEIGHT', 'SYSTEM', NOW()),
('G', 'Gram', 'WEIGHT', 'SYSTEM', NOW()),
('MG', 'Milligram', 'WEIGHT', 'SYSTEM', NOW()),
('TON', 'Metric Ton', 'WEIGHT', 'SYSTEM', NOW()),
('LB', 'Pound', 'WEIGHT', 'SYSTEM', NOW()),
('OZ', 'Ounce', 'WEIGHT', 'SYSTEM', NOW()),

-- LENGTH
('KM', 'Kilometer', 'LENGTH', 'SYSTEM', NOW()),
('M', 'Meter', 'LENGTH', 'SYSTEM', NOW()),
('CM', 'Centimeter', 'LENGTH', 'SYSTEM', NOW()),
('MM', 'Millimeter', 'LENGTH', 'SYSTEM', NOW()),
('IN', 'Inch', 'LENGTH', 'SYSTEM', NOW()),
('FT', 'Foot', 'LENGTH', 'SYSTEM', NOW()),
('YD', 'Yard', 'LENGTH', 'SYSTEM', NOW()),
('MI', 'Mile', 'LENGTH', 'SYSTEM', NOW()),

-- UNIT (Pieces, Box, etc)
('PCS', 'Pieces', 'UNIT', 'SYSTEM', NOW()),
('BOX', 'Box', 'UNIT', 'SYSTEM', NOW()),
('DOZ', 'Dozen', 'UNIT', 'SYSTEM', NOW()),
('ROLL', 'Roll', 'UNIT', 'SYSTEM', NOW()),
('PACK', 'Pack', 'UNIT', 'SYSTEM', NOW()),
('SET', 'Set', 'UNIT', 'SYSTEM', NOW()),
('BTL', 'Bottle', 'UNIT', 'SYSTEM', NOW()),

-- VOLUME
('L', 'Liter', 'VOLUME', 'SYSTEM', NOW()),
('ML', 'Milliliter', 'VOLUME', 'SYSTEM', NOW()),
('GAL', 'Gallon', 'VOLUME', 'SYSTEM', NOW()),
('BBL', 'Barrel', 'VOLUME', 'SYSTEM', NOW()),
('M3', 'Cubic Meter', 'VOLUME', 'SYSTEM', NOW()),
('CM3', 'Cubic Centimeter', 'VOLUME', 'SYSTEM', NOW()),

-- TIME
('HR', 'Hour', 'TIME', 'SYSTEM', NOW()),
('MIN', 'Minute', 'TIME', 'SYSTEM', NOW()),
('SEC', 'Second', 'TIME', 'SYSTEM', NOW()),
('DAY', 'Day', 'TIME', 'SYSTEM', NOW()),
('MO', 'Month', 'TIME', 'SYSTEM', NOW()),
('YR', 'Year', 'TIME', 'SYSTEM', NOW()),

-- AREA
('HA', 'Hectare', 'AREA', 'SYSTEM', NOW()),
('M2', 'Square Meter', 'AREA', 'SYSTEM', NOW()),
('KM2', 'Square Kilometer', 'AREA', 'SYSTEM', NOW()),
('AC', 'Acre', 'AREA', 'SYSTEM', NOW()),
('SQFT', 'Square Foot', 'AREA', 'SYSTEM', NOW()),
('SQIN', 'Square Inch', 'AREA', 'SYSTEM', NOW());
