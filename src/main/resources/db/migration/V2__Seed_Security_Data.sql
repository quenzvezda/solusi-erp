-- V2: Seed Security & RBAC Data
-- Password placeholder: INITIAL_PASSWORD_SETUP (will be updated by SystemInitializer)

INSERT INTO permissions (name, description, created_by, created_date) VALUES 
('DASHBOARD_READ', 'Akses Halaman Dashboard Utama', 'SYSTEM', NOW()),
('USERS_READ', 'Melihat Daftar Pengguna', 'SYSTEM', NOW()),
('USERS_CREATE', 'Menambah Pengguna Baru', 'SYSTEM', NOW()),
('USERS_UPDATE', 'Mengubah Data Pengguna', 'SYSTEM', NOW()),
('USERS_DELETE', 'Menghapus Pengguna', 'SYSTEM', NOW()),
('ROLES_READ', 'Melihat Daftar Role', 'SYSTEM', NOW()),
('ROLES_CREATE', 'Menambah Role Baru', 'SYSTEM', NOW()),
('ROLES_UPDATE', 'Mengubah Data Role', 'SYSTEM', NOW()),
('ROLES_DELETE', 'Menghapus Role', 'SYSTEM', NOW()),
('PERMISSIONS_READ', 'Melihat Daftar Permission', 'SYSTEM', NOW()),
('PERMISSIONS_CREATE', 'Menambah Permission Baru', 'SYSTEM', NOW()),
('PERMISSIONS_UPDATE', 'Mengubah Data Permission', 'SYSTEM', NOW()),
('PERMISSIONS_DELETE', 'Menghapus Permission', 'SYSTEM', NOW());

INSERT INTO roles (name, description, created_by, created_date) VALUES 
('ROLE_ADMIN', 'Administrator Sistem dengan Akses Penuh', 'SYSTEM', NOW()),
('ROLE_STAFF', 'Staff Operasional dengan Akses Terbatas', 'SYSTEM', NOW());

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN';

-- Initial setup password
INSERT INTO users (username, password, email, enabled, password_change_required, role_id, created_by, created_date)
SELECT 'admin', 'INITIAL_PASSWORD_SETUP', 'admin@solusierp.com', TRUE, TRUE, id, 'SYSTEM', NOW()
FROM roles WHERE name = 'ROLE_ADMIN';

INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by, created_date)
SELECT id, 'Administrator Utama', '08123456789', 'id', 10, 'light', 'SYSTEM', NOW()
FROM users WHERE username = 'admin';
