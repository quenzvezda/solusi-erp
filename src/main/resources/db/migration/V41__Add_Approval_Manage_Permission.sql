-- V41: Add APPROVAL_MANAGE permission + sidebar menu entry, grant to ROLE_ADMIN

-- 1. Add permission group entry for "Manage Approvals" sidebar item
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, created_by_user_id, created_date)
VALUES (
    'COM-03',
    'Kelola Persetujuan', 'Manage Approvals',
    'Operasional > Aplikasi > Kelola Persetujuan',
    'Operations > Application > Manage Approvals',
    '/common/approval/manage',
    'ti-clipboard-list',
    'Kelola semua approval request (admin)', 'Manage all approval requests (admin)',
    1, NOW()
);

-- 2. Insert APPROVAL_MANAGE permission in COM-03 group
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id)
VALUES (
    'APPROVAL_MANAGE',
    'Mengelola semua approval request (admin only)',
    1, NOW(),
    (SELECT id FROM permission_groups WHERE code = 'COM-03')
);

-- 3. Grant APPROVAL_MANAGE to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name = 'APPROVAL_MANAGE';
