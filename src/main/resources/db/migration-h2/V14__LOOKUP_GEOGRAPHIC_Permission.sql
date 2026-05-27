-- V14: Master Module - Geographic - Popup Permission

-- 1. Insert Permission LOOKUP_GEOGRAPHIC
INSERT INTO permissions (name, description, created_by, created_date) 
VALUES ('LOOKUP_GEOGRAPHIC', 'Mencari dan Memilih Wilayah Geografis (Popup/Autocomplete)', 'SYSTEM', NOW());

-- 2. Add LOOKUP_GEOGRAPHIC to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name = 'LOOKUP_GEOGRAPHIC';
