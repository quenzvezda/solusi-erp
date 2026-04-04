-- DEV SEEDER: Comprehensive Development & Testing Data
-- Lokasi File: docs/database/dev-seeder.sql
-- Deskripsi: Data lengkap untuk development & testing termasuk:
--   - Party Role Type: APPROVER (idempotent, same as V37 migration)
--   - Security Roles: ROLE_APPROVER, ROLE_WAREHOUSE, ROLE_EMPLOYEE
--   - Parties: Admin, Approvers, Customers, Suppliers, Warehouse Operator, Employee
--   - Addresses, Contacts, Identifications untuk setiap Party
--   - Users dengan role dan party yang terhubung
--   - Inventory seed data (facilities, grids, containers, products)
--
-- Password semua user: admin123 (BCrypt encoded)
-- Jalankan setelah semua Flyway migration selesai.
--
-- USAGE:
--   docker exec -i mariadb-local mariadb -uroot -proot erp-test < docs/database/dev-seeder.sql

-- ============================================================
-- 0. CLEANUP (Idempotent — hapus data dev sebelumnya)
-- ============================================================
-- Inventory demo data
DELETE FROM products WHERE code LIKE 'PRD-DEMO-%';
DELETE FROM brands WHERE code LIKE 'BRD-DEMO-%';
DELETE FROM product_categories WHERE code LIKE 'CAT-DEMO-%';
DELETE FROM inv_containers WHERE code LIKE 'BIN-DEMO-%';
DELETE FROM inv_grids WHERE code LIKE 'GRD-DEMO-%';
DELETE FROM inv_facilities WHERE code LIKE 'FAC-DEMO-%';

-- Approval data (clean slate for testing)
DELETE FROM appr_signatures;
DELETE FROM appr_histories;
DELETE FROM appr_requests;

-- News data (table created by app on first run — skip if not exists)
SET @news_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'news');
SET @sql = IF(@news_exists > 0, 'DELETE FROM news', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- User-Party links for dev users (excluding admin=1)
UPDATE users SET party_id = NULL WHERE username IN ('approver1','approver2','warehouse1','employee1');

-- User profiles for dev users
DELETE FROM user_profiles WHERE user_id IN (SELECT id FROM users WHERE username IN ('approver1','approver2','warehouse1','employee1'));

-- Dev users
DELETE FROM users WHERE username IN ('approver1','approver2','warehouse1','employee1');

-- Party cleanup (dev codes)
DELETE FROM party_address_types WHERE party_address_id IN (SELECT id FROM party_addresses WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01'));
DELETE FROM party_contacts WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01');
DELETE FROM party_identifications WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01');
DELETE FROM party_addresses WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01');
DELETE FROM party_roles WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01');
DELETE FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01';

-- Role permissions for dev roles
DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM roles WHERE name IN ('ROLE_APPROVER','ROLE_WAREHOUSE','ROLE_EMPLOYEE'));
DELETE FROM roles WHERE name IN ('ROLE_APPROVER','ROLE_WAREHOUSE','ROLE_EMPLOYEE');

-- Remove legacy ROLE_STAFF if still exists
DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM roles WHERE name = 'ROLE_STAFF');
DELETE FROM roles WHERE name = 'ROLE_STAFF';

-- ============================================================
-- 1. PARTY ROLE TYPE: APPROVER (idempotent)
-- ============================================================
INSERT INTO party_role_types (code, name, note, is_active, created_by_user_id, created_date, version)
SELECT 'APPROVER', 'Approver', 'Party yang berwenang menyetujui dokumen', 1, 1, NOW(), 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM party_role_types WHERE code = 'APPROVER');

-- ============================================================
-- 2. SECURITY ROLES
-- ============================================================
-- BCrypt hash for "admin123"
SET @pwd = '$2b$10$kJlPG9rZovRB57u0POwoQujVA0EK4kI6qWpUZJD/O8fa5WxfyAGca';

INSERT INTO roles (name, description, created_by_user_id, created_date, version) VALUES
('ROLE_APPROVER', 'Approver — dapat mereview dan menyetujui dokumen', 1, NOW(), 1),
('ROLE_WAREHOUSE', 'Warehouse Operator — mengelola stok dan gudang', 1, NOW(), 1),
('ROLE_EMPLOYEE', 'Karyawan — akses dasar untuk membaca berita dan dashboard', 1, NOW(), 1);

SET @role_admin_id    = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');
SET @role_approver_id = (SELECT id FROM roles WHERE name = 'ROLE_APPROVER');
SET @role_warehouse_id = (SELECT id FROM roles WHERE name = 'ROLE_WAREHOUSE');
SET @role_employee_id = (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE');

-- ============================================================
-- 3. ROLE PERMISSIONS
-- ============================================================
-- ROLE_APPROVER: Dashboard + News + Approval + Party Lookup
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_approver_id, id FROM permissions WHERE name IN (
    'DASHBOARD_READ',
    'NEWS_READ', 'NEWS_CREATE', 'NEWS_UPDATE',
    'APPROVAL_READ', 'APPROVAL_PROCESS',
    'LOOKUP_PARTY',
    'DASHBOARD_NEWS', 'DASHBOARD_APPROVAL'
);

-- ROLE_WAREHOUSE: Dashboard + Inventory Management
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_warehouse_id, id FROM permissions WHERE name IN (
    'DASHBOARD_READ',
    'FACILITY_READ', 'GRID_READ', 'CONTAINER_READ',
    'STOCK-ADJUSTMENT_READ', 'STOCK-ADJUSTMENT_CREATE', 'STOCK-ADJUSTMENT_UPDATE', 'STOCK-ADJUSTMENT_PROCESS',
    'STOCK-CARD_READ', 'ON-HAND_READ',
    'PRODUCT_READ', 'BRAND_READ', 'PRODUCT-CATEGORY_READ', 'UNIT-OF-MEASURE_READ',
    'LOOKUP_BRAND', 'LOOKUP_PRODUCT-CATEGORY', 'LOOKUP_FACILITY', 'LOOKUP_GRID', 'LOOKUP_CONTAINER',
    'LOOKUP_INVENTORY', 'LOOKUP_UOM-CONVERSION'
);

-- ROLE_EMPLOYEE: Dashboard + News read-only
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_employee_id, id FROM permissions WHERE name IN (
    'DASHBOARD_READ',
    'NEWS_READ',
    'DASHBOARD_NEWS'
);

-- ============================================================
-- 4. PARTIES
-- ============================================================
-- Party Role Type IDs
SET @prt_internal   = (SELECT id FROM party_role_types WHERE code = 'INTERNAL');
SET @prt_customer   = (SELECT id FROM party_role_types WHERE code = 'CUSTOMER');
SET @prt_supplier   = (SELECT id FROM party_role_types WHERE code = 'SUPPLIER');
SET @prt_employee   = (SELECT id FROM party_role_types WHERE code = 'EMPLOYEE');
SET @prt_warehouse  = (SELECT id FROM party_role_types WHERE code = 'WAREHOUSE_OPERATOR');
SET @prt_approver   = (SELECT id FROM party_role_types WHERE code = 'APPROVER');

-- ID Type IDs
SET @idt_ktp     = (SELECT id FROM party_id_types WHERE code = 'KTP');
SET @idt_npwp    = (SELECT id FROM party_id_types WHERE code = 'NPWP');
SET @idt_nib     = (SELECT id FROM party_id_types WHERE code = 'NIB');
SET @idt_passport = (SELECT id FROM party_id_types WHERE code = 'PASSPORT');

-- Geographic IDs
SET @geo_jkt_pusat   = 213; -- Kota Adm. Jakarta Pusat
SET @geo_jkt_selatan = 216; -- Kota Adm. Jakarta Selatan
SET @geo_jkt_timur   = 217; -- Kota Adm. Jakarta Timur

-- ────────────────────────────────────────────────────────────
-- 4A. Party: Solusi Program (Organization — Internal Owner)
-- ────────────────────────────────────────────────────────────
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('PRT-INTERNAL-01', 'PT.', 'Solusi Program', 'ORGANIZATION', 1, 'info@solusierp.com', '021-5551000', 1, NOW(), 1, NOW(), 1);
SET @p_owner = (SELECT id FROM parties WHERE code = 'PRT-INTERNAL-01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_owner, @prt_internal);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_owner, 'Jl. Sudirman No. 1, Gedung Solusi Lt. 5', @geo_jkt_pusat, '10220', 1, 1, 1, NOW(), 1);
SET @addr_owner = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_owner, 'OFFICE');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_owner, 'Kantor Pusat', NULL, '021-5551000', 'info@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_owner, @idt_npwp, '01.234.567.8-012.000', '2020-01-15', 1, 1, 1, NOW(), 1);
INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_owner, @idt_nib, '9120012345678', '2020-03-01', 1, 0, 1, NOW(), 1);

-- ────────────────────────────────────────────────────────────
-- 4B. Party: Administrator (Person — linked to admin user)
-- ────────────────────────────────────────────────────────────
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-ADMIN', 'Bpk.', 'Administrator Utama', 'PERSON', 1, 'admin@solusierp.com', '081234567890', 1, NOW(), 1, NOW(), 1);
SET @p_admin = (SELECT id FROM parties WHERE code = 'BP-DEV-ADMIN');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_admin, @prt_internal), (@p_admin, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_admin, 'Jl. Sudirman No. 1, Gedung Solusi Lt. 5', @geo_jkt_pusat, '10220', 1, 1, 1, NOW(), 1);
SET @addr_admin = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_admin, 'OFFICE');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_admin, 'Personal', '081234567890', NULL, 'admin@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_admin, @idt_ktp, '3171012345670001', '2020-06-15', 1, 1, 1, NOW(), 1);

-- Link admin user to party
UPDATE users SET party_id = @p_admin WHERE username = 'admin';

-- ────────────────────────────────────────────────────────────
-- 4C. Party: Budi Santoso (Approver 1)
-- ────────────────────────────────────────────────────────────
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-APR01', 'Bpk.', 'Budi Santoso', 'PERSON', 1, 'budi.santoso@solusierp.com', '081234567891', 1, NOW(), 1, NOW(), 1);
SET @p_apr1 = (SELECT id FROM parties WHERE code = 'BP-DEV-APR01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_apr1, @prt_approver), (@p_apr1, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr1, 'Jl. Gatot Subroto No. 45, Pancoran', @geo_jkt_selatan, '12780', 1, 1, 1, NOW(), 1);
SET @addr_apr1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_apr1, 'HOME');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr1, 'Personal', '081234567891', NULL, 'budi.santoso@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr1, @idt_ktp, '3171012345670002', '2021-03-10', 1, 1, 1, NOW(), 1);

-- ────────────────────────────────────────────────────────────
-- 4D. Party: Siti Rahayu (Approver 2)
-- ────────────────────────────────────────────────────────────
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-APR02', 'Ibu', 'Siti Rahayu', 'PERSON', 1, 'siti.rahayu@solusierp.com', '081234567892', 1, NOW(), 1, NOW(), 1);
SET @p_apr2 = (SELECT id FROM parties WHERE code = 'BP-DEV-APR02');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_apr2, @prt_approver), (@p_apr2, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr2, 'Jl. Casablanca No. 12, Tebet', @geo_jkt_selatan, '12870', 1, 1, 1, NOW(), 1);
SET @addr_apr2 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_apr2, 'HOME');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr2, 'Personal', '081234567892', NULL, 'siti.rahayu@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr2, @idt_ktp, '3171012345670003', '2021-07-22', 1, 1, 1, NOW(), 1);

-- ────────────────────────────────────────────────────────────
-- 4E. Party: PT. Maju Jaya (Customer 1)
-- ────────────────────────────────────────────────────────────
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, notes, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-CUST01', 'PT.', 'Maju Jaya', 'ORGANIZATION', 1, 'purchasing@majujaya.co.id', '021-5552001', 'Customer utama sektor retail', 1, NOW(), 1, NOW(), 1);
SET @p_cust1 = (SELECT id FROM parties WHERE code = 'BP-DEV-CUST01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_cust1, @prt_customer);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust1, 'Jl. Mangga Dua Raya No. 88, Pademangan', @geo_jkt_pusat, '10730', 1, 1, 1, NOW(), 1);
SET @addr_cust1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_cust1, 'OFFICE'), (@addr_cust1, 'BILLING');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust1, 'PIC Purchasing', '081299887766', '021-5552001', 'purchasing@majujaya.co.id', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust1, @idt_npwp, '02.345.678.9-013.000', '2019-05-20', 1, 1, 1, NOW(), 1);
INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust1, @idt_nib, '9120098765432', '2019-06-01', 1, 0, 1, NOW(), 1);

-- ────────────────────────────────────────────────────────────
-- 4F. Party: CV. Berkah Sejahtera (Customer 2)
-- ────────────────────────────────────────────────────────────
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, notes, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-CUST02', 'CV.', 'Berkah Sejahtera', 'ORGANIZATION', 1, 'order@berkahsejahtera.id', '021-5553002', 'Customer UMKM sektor F&B', 1, NOW(), 1, NOW(), 1);
SET @p_cust2 = (SELECT id FROM parties WHERE code = 'BP-DEV-CUST02');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_cust2, @prt_customer);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust2, 'Jl. Kalibata Raya No. 5, Pancoran', @geo_jkt_selatan, '12740', 1, 1, 1, NOW(), 1);
SET @addr_cust2 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_cust2, 'OFFICE');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust2, 'Owner', '081388776655', NULL, 'order@berkahsejahtera.id', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust2, @idt_npwp, '03.456.789.0-014.000', '2022-01-10', 1, 1, 1, NOW(), 1);

-- ────────────────────────────────────────────────────────────
-- 4G. Party: PT. Sumber Makmur (Supplier)
-- ────────────────────────────────────────────────────────────
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, notes, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-SUP01', 'PT.', 'Sumber Makmur', 'ORGANIZATION', 1, 'sales@sumbermakmur.co.id', '021-5554003', 'Supplier utama elektronik dan gadget', 1, NOW(), 1, NOW(), 1);
SET @p_sup1 = (SELECT id FROM parties WHERE code = 'BP-DEV-SUP01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_sup1, @prt_supplier);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, 'Kawasan Industri Pulogadung Blok A No. 10', @geo_jkt_timur, '13920', 1, 1, 1, NOW(), 1);
SET @addr_sup1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_sup1, 'FACTORY'), (@addr_sup1, 'SHIPPING');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, 'PIC Sales', '081277665544', '021-5554003', 'sales@sumbermakmur.co.id', 1, 1, 1, NOW(), 1);
INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, 'Gudang', NULL, '021-5554004', 'gudang@sumbermakmur.co.id', 1, 0, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, @idt_npwp, '04.567.890.1-015.000', '2018-08-15', 1, 1, 1, NOW(), 1);
INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, @idt_nib, '9120011223344', '2018-09-01', 1, 0, 1, NOW(), 1);

-- ────────────────────────────────────────────────────────────
-- 4H. Party: Ahmad Fadli (Warehouse Operator)
-- ────────────────────────────────────────────────────────────
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-WH01', 'Bpk.', 'Ahmad Fadli', 'PERSON', 1, 'ahmad.fadli@solusierp.com', '081234567893', 1, NOW(), 1, NOW(), 1);
SET @p_wh1 = (SELECT id FROM parties WHERE code = 'BP-DEV-WH01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_wh1, @prt_warehouse), (@p_wh1, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_wh1, 'Jl. Cempaka Putih Tengah No. 20', @geo_jkt_pusat, '10510', 1, 1, 1, NOW(), 1);
SET @addr_wh1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_wh1, 'HOME');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_wh1, 'Personal', '081234567893', NULL, 'ahmad.fadli@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_wh1, @idt_ktp, '3171012345670004', '2022-01-05', 1, 1, 1, NOW(), 1);

-- ────────────────────────────────────────────────────────────
-- 4I. Party: Dewi Lestari (Employee)
-- ────────────────────────────────────────────────────────────
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-EMP01', 'Ibu', 'Dewi Lestari', 'PERSON', 1, 'dewi.lestari@solusierp.com', '081234567894', 1, NOW(), 1, NOW(), 1);
SET @p_emp1 = (SELECT id FROM parties WHERE code = 'BP-DEV-EMP01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_emp1, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_emp1, 'Jl. Kebagusan Raya No. 15, Pasar Minggu', @geo_jkt_selatan, '12520', 1, 1, 1, NOW(), 1);
SET @addr_emp1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_emp1, 'HOME');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_emp1, 'Personal', '081234567894', NULL, 'dewi.lestari@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_emp1, @idt_ktp, '3171012345670005', '2023-02-14', 1, 1, 1, NOW(), 1);

-- ============================================================
-- 5. USERS (linked to parties)
-- ============================================================
INSERT INTO users (username, password, email, enabled, password_change_required, role_id, party_id, created_by_user_id, created_date, version) VALUES
('approver1', @pwd, 'budi.santoso@solusierp.com', 1, 0, @role_approver_id, @p_apr1, 1, NOW(), 1),
('approver2', @pwd, 'siti.rahayu@solusierp.com',  1, 0, @role_approver_id, @p_apr2, 1, NOW(), 1),
('warehouse1', @pwd, 'ahmad.fadli@solusierp.com', 1, 0, @role_warehouse_id, @p_wh1, 1, NOW(), 1),
('employee1', @pwd, 'dewi.lestari@solusierp.com', 1, 0, @role_employee_id, @p_emp1, 1, NOW(), 1);

-- ============================================================
-- 6. USER PROFILES
-- ============================================================
INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by_user_id, created_date, version)
SELECT id, 'Budi Santoso', '081234567891', 'id', 10, 'light', 1, NOW(), 1 FROM users WHERE username = 'approver1';

INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by_user_id, created_date, version)
SELECT id, 'Siti Rahayu', '081234567892', 'id', 10, 'light', 1, NOW(), 1 FROM users WHERE username = 'approver2';

INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by_user_id, created_date, version)
SELECT id, 'Ahmad Fadli', '081234567893', 'id', 10, 'light', 1, NOW(), 1 FROM users WHERE username = 'warehouse1';

INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by_user_id, created_date, version)
SELECT id, 'Dewi Lestari', '081234567894', 'id', 10, 'light', 1, NOW(), 1 FROM users WHERE username = 'employee1';

-- ============================================================
-- 7. SEED FACILITY (uses Internal Owner party)
-- ============================================================
INSERT INTO inv_facilities (code, name, owner_id, city_id, address_line1, postal_code, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('FAC-DEMO-01', 'Gudang Utama Jakarta Pusat', @p_owner, @geo_jkt_pusat, 'Jl. Medan Merdeka Barat No. 1, Gambir', '10110', 1, 1, 1, NOW(), 1, NOW()),
('FAC-DEMO-02', 'Gudang Transit Jakarta', @p_owner, @geo_jkt_pusat, 'Jl. Gajah Mada No. 18', '10120', 1, 1, 1, NOW(), 1, NOW());

SET @fac_main_id = (SELECT id FROM inv_facilities WHERE code = 'FAC-DEMO-01');

-- ============================================================
-- 8. SEED GRID (Zones)
-- ============================================================
INSERT INTO inv_grids (facility_id, code, name, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
(@fac_main_id, 'GRD-DEMO-A', 'Area A - Elektronik', 'Khusus barang elektronik & gadget', 1, 1, 1, NOW(), 1, NOW()),
(@fac_main_id, 'GRD-DEMO-B', 'Area B - Furnitur', 'Barang berukuran besar', 1, 1, 1, NOW(), 1, NOW()),
(@fac_main_id, 'GRD-DEMO-C', 'Area C - Pendingin', 'Suhu terkontrol 0-5°C', 1, 1, 1, NOW(), 1, NOW());

SET @grid_a_id = (SELECT id FROM inv_grids WHERE code = 'GRD-DEMO-A' AND facility_id = @fac_main_id);

-- ============================================================
-- 9. SEED CONTAINER (Bins/Racks)
-- ============================================================
INSERT INTO inv_containers (grid_id, code, name, barcode, length, width, height, max_weight, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
(@grid_a_id, 'BIN-DEMO-A01-01', 'Rak Elektronik A-01-01', 'BC-DEMO-A0101', 50.00, 50.00, 50.00, 100.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_a_id, 'BIN-DEMO-A01-02', 'Rak Elektronik A-01-02', 'BC-DEMO-A0102', 50.00, 50.00, 50.00, 100.00, 1, 1, 1, NOW(), 1, NOW()),
(@grid_a_id, 'BIN-DEMO-A02-01', 'Rak Elektronik A-02-01', 'BC-DEMO-A0201', 100.00, 100.00, 100.00, 500.00, 1, 1, 1, NOW(), 1, NOW());

-- ============================================================
-- 10. SEED PRODUCT CATEGORY
-- ============================================================
INSERT INTO product_categories (code, name, type, note, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('CAT-DEMO-01', 'Smartphone', 'STOCK', 'Kategori untuk perangkat telepon pintar', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-02', 'Furniture', 'STOCK', 'Kategori untuk perabotan kantor dan rumah', 1, 1, NOW(), 1, NOW()),
('CAT-DEMO-03', 'Service', 'SERVICE', 'Kategori untuk jasa dan layanan', 1, 1, NOW(), 1, NOW());

SET @cat_smartphone_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-01');
SET @cat_furniture_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-02');
SET @cat_service_id = (SELECT id FROM product_categories WHERE code = 'CAT-DEMO-03');

-- ============================================================
-- 11. SEED BRAND
-- ============================================================
INSERT INTO brands (code, name, note, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('BRD-DEMO-01', 'Samsung', 'Brand elektronik Korea Selatan', 1, 1, NOW(), 1, NOW()),
('BRD-DEMO-02', 'IKEA', 'Brand furnitur Swedia', 1, 1, NOW(), 1, NOW()),
('BRD-DEMO-03', 'Generic', 'Brand umum tanpa merk spesifik', 1, 1, NOW(), 1, NOW());

SET @brd_samsung_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-01');
SET @brd_ikea_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-02');
SET @brd_generic_id = (SELECT id FROM brands WHERE code = 'BRD-DEMO-03');

-- ============================================================
-- 12. SEED PRODUCT
-- ============================================================
SET @uom_pcs_id = (SELECT id FROM unit_of_measures WHERE code = 'PCS');

INSERT INTO products (code, name, barcode, category_id, uom_id, brand_id, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('PRD-DEMO-0001', 'Samsung Galaxy S24 Ultra', '8806095300001', @cat_smartphone_id, @uom_pcs_id, @brd_samsung_id, 1, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0002', 'IKEA Billy Bookcase White', '20135268', @cat_furniture_id, @uom_pcs_id, @brd_ikea_id, 1, 1, 1, NOW(), 1, NOW()),
('PRD-DEMO-0003', 'Laptop Cleaning Service', NULL, @cat_service_id, @uom_pcs_id, @brd_generic_id, 1, 1, 1, NOW(), 1, NOW());

-- ============================================================
-- SUMMARY
-- ============================================================
-- Users (password: admin123):
--   admin      / ROLE_ADMIN     / BP-DEV-ADMIN  (Administrator Utama)
--   approver1  / ROLE_APPROVER  / BP-DEV-APR01  (Budi Santoso)
--   approver2  / ROLE_APPROVER  / BP-DEV-APR02  (Siti Rahayu)
--   warehouse1 / ROLE_WAREHOUSE / BP-DEV-WH01   (Ahmad Fadli)
--   employee1  / ROLE_EMPLOYEE  / BP-DEV-EMP01  (Dewi Lestari)
--
-- Parties (without user account):
--   PRT-INTERNAL-01 / PT. Solusi Program  (Internal Owner)
--   BP-DEV-CUST01   / PT. Maju Jaya       (Customer)
--   BP-DEV-CUST02   / CV. Berkah Sejahtera (Customer)
--   BP-DEV-SUP01    / PT. Sumber Makmur   (Supplier)


