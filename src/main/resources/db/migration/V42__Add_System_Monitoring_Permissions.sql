-- V42: Add System Monitoring permission group, permissions, and grant to ROLE_ADMIN

-- 1. Insert Permission Group for System Monitoring
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES (
    'SYS-01',
    'Monitoring Sistem', 'System Monitoring',
    'Sistem > Monitoring', 'System > Monitoring',
    '/monitoring',
    'ti-activity',
    'Pantau kesehatan aplikasi dan baca log secara real-time', 'Monitor application health and read logs in real-time',
    90,
    1, NOW()
);

-- 2. Insert permissions
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('MONITORING_READ', 'Melihat halaman monitoring: health status dan log aplikasi', 1, NOW(),
    (SELECT id FROM permission_groups WHERE code = 'SYS-01')),
('MONITORING_DOWNLOAD', 'Mengunduh file log aplikasi', 1, NOW(),
    (SELECT id FROM permission_groups WHERE code = 'SYS-01'));

-- 3. Grant to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name LIKE 'MONITORING\_%' ESCAPE '\\';
