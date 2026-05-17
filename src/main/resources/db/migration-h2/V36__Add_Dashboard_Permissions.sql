-- V36: Add Dashboard card permissions for News and Approval cards
-- These permissions control visibility of dashboard cards (DASHBOARD_NEWS, DASHBOARD_APPROVAL)

-- 1. Insert Dashboard Permission Group (DASH-01)
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, created_by_user_id, created_date) VALUES
('DASH-01', 'Dashboard', 'Dashboard', 'Dashboard', 'Dashboard', '/dashboard', 'ti-layout-dashboard', 'Kontrol visibilitas card dashboard', 'Control dashboard card visibility', 1, NOW());

-- 2. Insert Dashboard card permissions
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('DASHBOARD_NEWS', 'Menampilkan card berita terbaru di dashboard', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'DASH-01')),
('DASHBOARD_APPROVAL', 'Menampilkan card jumlah persetujuan pending di dashboard', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'DASH-01'));

-- 3. Grant to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name IN ('DASHBOARD_NEWS', 'DASHBOARD_APPROVAL');
