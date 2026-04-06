-- V37: Remove ROLE_STAFF (unused legacy role) & add APPROVER party role type
-- ==========================================================================

-- 1. Remove ROLE_STAFF
-- No user should have ROLE_STAFF assigned (only admin has ROLE_ADMIN)
DELETE FROM role_permissions WHERE role_id = (SELECT id FROM roles WHERE name = 'ROLE_STAFF');
DELETE FROM roles WHERE name = 'ROLE_STAFF';

-- 2. Add APPROVER party role type
INSERT INTO party_role_types (code, name, note, is_active, created_by_user_id, created_date, version)
SELECT 'APPROVER', 'Approver', 'Party yang berwenang menyetujui dokumen', 1, 1, NOW(), 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM party_role_types WHERE code = 'APPROVER');
