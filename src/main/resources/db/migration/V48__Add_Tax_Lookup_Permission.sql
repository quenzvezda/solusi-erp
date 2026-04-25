INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id)
VALUES (
    'LOOKUP_TAX',
    'Lookup tax untuk autocomplete procurement',
    1,
    NOW(),
    (SELECT id FROM permission_groups WHERE code = 'PUR-03')
)
ON DUPLICATE KEY UPDATE name = name;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name = 'LOOKUP_TAX';
