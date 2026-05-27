-- V32: Add missing LOOKUP_UOM-CONVERSION permission and grant to ROLE_ADMIN
-- Bug Fix: InventoryApiLookupController requires 'LOOKUP_UOM-CONVERSION' authority
-- but this permission was never seeded (V25 only added UOM-CONVERSION CRUD permissions,
-- V31 added other LOOKUP_* permissions but omitted this one).
-- Standard: AGENTS.md Section 12 — Prefix 'LOOKUP_' for autocomplete/popup shared features.

INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('LOOKUP_UOM-CONVERSION', 'Mencari dan Melihat Konversi UoM Produk (Autocomplete)', 1, NOW(),
    (SELECT id FROM permission_groups WHERE code = 'INV-11'));

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name = 'LOOKUP_UOM-CONVERSION';
