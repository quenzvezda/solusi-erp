-- V35: Add News and Approval permission groups + permissions and grant to ROLE_ADMIN
-- Adds permission_groups entries for News (COM-01) and Approval (COM-02)
-- Inserts permissions and grants them to ROLE_ADMIN

-- 1. Insert Permission Groups
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, created_by_user_id, created_date) VALUES
('COM-01', 'Berita', 'News', 'Operasional > Aplikasi > Berita', 'Operations > Application > News', '/common/news', 'ti-news', 'Manajemen konten berita', 'Manage news content', 1, NOW()),
('COM-02', 'Persetujuan', 'Approvals', 'Operasional > Aplikasi > Persetujuan', 'Operations > Application > Approvals', '/common/approval', 'ti-checkbox', 'Manajemen persetujuan', 'Manage generic approvals', 1, NOW());

-- 2. Insert Permissions for News
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('NEWS_READ', 'Melihat daftar dan detail berita', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'COM-01')),
('NEWS_CREATE', 'Membuat berita baru', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'COM-01')),
('NEWS_UPDATE', 'Mengubah berita', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'COM-01'));

-- 3. Insert Permissions for Approval
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('APPROVAL_READ', 'Melihat timeline/riwayat persetujuan', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'COM-02')),
('APPROVAL_PROCESS', 'Memproses approval (approve/reject dan menyimpan signature)', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'COM-02'));

-- 4. Grant permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name IN ('NEWS_READ','NEWS_CREATE','NEWS_UPDATE','APPROVAL_READ','APPROVAL_PROCESS');
