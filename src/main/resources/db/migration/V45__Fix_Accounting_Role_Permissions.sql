-- V44: Fix missing ROLE_ADMIN permissions for Accounting Foundation
--
-- Root cause: V43 used LIKE 'ACCOUNTING-\_%' ESCAPE '\\' which matches
-- 'ACCOUNTING-_*' (literal underscore after hyphen), but actual permission
-- names follow the pattern 'ACCOUNTING-GROUP_ACTION' (e.g. ACCOUNTING-COA_READ).
-- The wrong LIKE pattern inserted 0 rows into role_permissions for ROLE_ADMIN.
--
-- This migration grants all missing accounting permissions to ROLE_ADMIN.

-- Grant all ACCOUNTING-* permissions (COA, SCHEMA, PERIOD) to ROLE_ADMIN
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name LIKE 'ACCOUNTING-%';

-- Grant LOOKUP_COA to ROLE_ADMIN (was also missing)
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name = 'LOOKUP_COA';
