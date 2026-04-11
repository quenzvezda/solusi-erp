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

-- News data
DELETE FROM common_news WHERE 1=1;

-- Reset admin password_change_required so login works immediately after seeding
UPDATE users SET password_change_required = 0 WHERE username = 'admin';

-- User-Party links for dev users (including admin to allow party cleanup)
UPDATE users SET party_id = NULL WHERE username IN ('admin','approver1','approver2','warehouse1','employee1');

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

-- ============================================================
-- ACCOUNTING FOUNDATION (GL Seeder)
-- Fiscal Years, Accounting Periods, Chart of Accounts
-- ============================================================

-- ------------------------------------------------------------
-- CLEANUP — GL data (idempotent, run-safe)
-- ------------------------------------------------------------
DELETE FROM acc_accounting_schemas WHERE 1=1;
DELETE FROM acc_accounting_periods WHERE fiscal_year_id IN (SELECT id FROM acc_fiscal_years WHERE code LIKE 'FY-%');
DELETE FROM acc_fiscal_years WHERE code LIKE 'FY-%';
DELETE FROM acc_chart_of_accounts WHERE code REGEXP '^[0-9]';

-- ------------------------------------------------------------
-- FISCAL YEARS
-- ------------------------------------------------------------
INSERT INTO acc_fiscal_years (code, name, start_date, end_date, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date) VALUES
('FY-2024', 'Fiscal Year 2024', '2024-01-01', '2024-12-31', 1, 1, 1, NOW(), 1, NOW()),
('FY-2025', 'Fiscal Year 2025', '2025-01-01', '2025-12-31', 1, 1, 1, NOW(), 1, NOW());

-- ------------------------------------------------------------
-- ACCOUNTING PERIODS — FY-2024 (12 monthly periods)
-- ------------------------------------------------------------
SET @fy2024 = (SELECT id FROM acc_fiscal_years WHERE code = 'FY-2024');

INSERT INTO acc_accounting_periods (code, name, period_number, fiscal_year_id, start_date, end_date, status, version, created_by_user_id, created_date, updated_by_user_id, updated_date) VALUES
('AP-2024-M01', 'January 2024',   1,  @fy2024, '2024-01-01', '2024-01-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M02', 'February 2024',  2,  @fy2024, '2024-02-01', '2024-02-29', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M03', 'March 2024',     3,  @fy2024, '2024-03-01', '2024-03-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M04', 'April 2024',     4,  @fy2024, '2024-04-01', '2024-04-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M05', 'May 2024',       5,  @fy2024, '2024-05-01', '2024-05-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M06', 'June 2024',      6,  @fy2024, '2024-06-01', '2024-06-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M07', 'July 2024',      7,  @fy2024, '2024-07-01', '2024-07-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M08', 'August 2024',    8,  @fy2024, '2024-08-01', '2024-08-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M09', 'September 2024', 9,  @fy2024, '2024-09-01', '2024-09-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M10', 'October 2024',   10, @fy2024, '2024-10-01', '2024-10-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M11', 'November 2024',  11, @fy2024, '2024-11-01', '2024-11-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M12', 'December 2024',  12, @fy2024, '2024-12-01', '2024-12-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW());

-- ------------------------------------------------------------
-- ACCOUNTING PERIODS — FY-2025 (12 monthly periods)
-- ------------------------------------------------------------
SET @fy2025 = (SELECT id FROM acc_fiscal_years WHERE code = 'FY-2025');

INSERT INTO acc_accounting_periods (code, name, period_number, fiscal_year_id, start_date, end_date, status, version, created_by_user_id, created_date, updated_by_user_id, updated_date) VALUES
('AP-2025-M01', 'January 2025',   1,  @fy2025, '2025-01-01', '2025-01-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M02', 'February 2025',  2,  @fy2025, '2025-02-01', '2025-02-28', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M03', 'March 2025',     3,  @fy2025, '2025-03-01', '2025-03-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M04', 'April 2025',     4,  @fy2025, '2025-04-01', '2025-04-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M05', 'May 2025',       5,  @fy2025, '2025-05-01', '2025-05-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M06', 'June 2025',      6,  @fy2025, '2025-06-01', '2025-06-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M07', 'July 2025',      7,  @fy2025, '2025-07-01', '2025-07-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M08', 'August 2025',    8,  @fy2025, '2025-08-01', '2025-08-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M09', 'September 2025', 9,  @fy2025, '2025-09-01', '2025-09-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M10', 'October 2025',   10, @fy2025, '2025-10-01', '2025-10-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M11', 'November 2025',  11, @fy2025, '2025-11-01', '2025-11-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M12', 'December 2025',  12, @fy2025, '2025-12-01', '2025-12-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW());

-- ------------------------------------------------------------
-- CHART OF ACCOUNTS — LEVEL 1 (Root / Main Categories)
-- account_type HEADER accounts: group nodes, not postable
-- ------------------------------------------------------------
INSERT INTO acc_chart_of_accounts (code, name, account_type, normal_balance, parent_id, level, is_header, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date) VALUES
('1000', 'ASSETS',      'ASSET',     'DEBIT',  NULL, 1, 1, 'All asset accounts',      1, 1, 1, NOW(), 1, NOW()),
('2000', 'LIABILITIES', 'LIABILITY', 'CREDIT', NULL, 1, 1, 'All liability accounts',  1, 1, 1, NOW(), 1, NOW()),
('3000', 'EQUITY',      'EQUITY',    'CREDIT', NULL, 1, 1, 'Owner equity accounts',   1, 1, 1, NOW(), 1, NOW()),
('4000', 'REVENUE',     'REVENUE',   'CREDIT', NULL, 1, 1, 'All revenue accounts',    1, 1, 1, NOW(), 1, NOW()),
('5000', 'EXPENSES',    'EXPENSE',   'DEBIT',  NULL, 1, 1, 'All expense accounts',    1, 1, 1, NOW(), 1, NOW());

-- Cache Level 1 IDs for parent references
SET @coa_assets      = (SELECT id FROM acc_chart_of_accounts WHERE code = '1000');
SET @coa_liabilities = (SELECT id FROM acc_chart_of_accounts WHERE code = '2000');
SET @coa_equity      = (SELECT id FROM acc_chart_of_accounts WHERE code = '3000');
SET @coa_revenue     = (SELECT id FROM acc_chart_of_accounts WHERE code = '4000');
SET @coa_expenses    = (SELECT id FROM acc_chart_of_accounts WHERE code = '5000');

-- ------------------------------------------------------------
-- CHART OF ACCOUNTS — LEVEL 2 (Sub-categories / Sub-headers)
-- ------------------------------------------------------------
INSERT INTO acc_chart_of_accounts (code, name, account_type, normal_balance, parent_id, level, is_header, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date) VALUES
-- Assets sub-groups
('1100', 'CURRENT ASSETS',         'ASSET',     'DEBIT',  @coa_assets,      2, 1, 'Short-term assets (liquid within 1 year)',  1, 1, 1, NOW(), 1, NOW()),
('1200', 'FIXED ASSETS',           'ASSET',     'DEBIT',  @coa_assets,      2, 1, 'Long-term tangible assets',                 1, 1, 1, NOW(), 1, NOW()),
-- Liability sub-groups
('2100', 'SHORT-TERM LIABILITIES', 'LIABILITY', 'CREDIT', @coa_liabilities, 2, 1, 'Obligations due within 1 year',            1, 1, 1, NOW(), 1, NOW()),
('2200', 'LONG-TERM LIABILITIES',  'LIABILITY', 'CREDIT', @coa_liabilities, 2, 1, 'Obligations due beyond 1 year',            1, 1, 1, NOW(), 1, NOW()),
-- Equity sub-groups
('3100', 'PAID-IN CAPITAL',        'EQUITY',    'CREDIT', @coa_equity,      2, 1, 'Capital contributed by shareholders',       1, 1, 1, NOW(), 1, NOW()),
('3200', 'RETAINED EARNINGS',      'EQUITY',    'CREDIT', @coa_equity,      2, 1, 'Accumulated profits/losses',                1, 1, 1, NOW(), 1, NOW()),
-- Revenue sub-groups
('4100', 'SALES REVENUE',          'REVENUE',   'CREDIT', @coa_revenue,     2, 1, 'Revenue from product sales',                1, 1, 1, NOW(), 1, NOW()),
('4200', 'SERVICE REVENUE',        'REVENUE',   'CREDIT', @coa_revenue,     2, 1, 'Revenue from services rendered',            1, 1, 1, NOW(), 1, NOW()),
-- Expense sub-groups
('5100', 'COST OF GOODS SOLD',     'EXPENSE',   'DEBIT',  @coa_expenses,    2, 1, 'Direct costs of goods sold',                1, 1, 1, NOW(), 1, NOW()),
('5200', 'OPERATING EXPENSES',     'EXPENSE',   'DEBIT',  @coa_expenses,    2, 1, 'Recurring operational expenses',            1, 1, 1, NOW(), 1, NOW());

-- Cache Level 2 IDs
SET @coa_current_assets    = (SELECT id FROM acc_chart_of_accounts WHERE code = '1100');
SET @coa_fixed_assets      = (SELECT id FROM acc_chart_of_accounts WHERE code = '1200');
SET @coa_short_liab        = (SELECT id FROM acc_chart_of_accounts WHERE code = '2100');
SET @coa_long_liab         = (SELECT id FROM acc_chart_of_accounts WHERE code = '2200');
SET @coa_paid_in_capital   = (SELECT id FROM acc_chart_of_accounts WHERE code = '3100');
SET @coa_retained_earnings = (SELECT id FROM acc_chart_of_accounts WHERE code = '3200');
SET @coa_sales_revenue     = (SELECT id FROM acc_chart_of_accounts WHERE code = '4100');
SET @coa_service_revenue   = (SELECT id FROM acc_chart_of_accounts WHERE code = '4200');
SET @coa_cogs              = (SELECT id FROM acc_chart_of_accounts WHERE code = '5100');
SET @coa_opex              = (SELECT id FROM acc_chart_of_accounts WHERE code = '5200');

-- ------------------------------------------------------------
-- CHART OF ACCOUNTS — LEVEL 3 (Detail / Postable Accounts)
-- is_header = 0: these accounts are used in journal entries
-- ------------------------------------------------------------
INSERT INTO acc_chart_of_accounts (code, name, account_type, normal_balance, parent_id, level, is_header, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date) VALUES
-- Current Assets (parent: 1100)
('1110', 'Cash - IDR',                         'ASSET',     'DEBIT',  @coa_current_assets,    3, 0, 'Petty cash and cash on hand in IDR',                 1, 1, 1, NOW(), 1, NOW()),
('1120', 'Bank - Primary Account',             'ASSET',     'DEBIT',  @coa_current_assets,    3, 0, 'Main operational bank account',                      1, 1, 1, NOW(), 1, NOW()),
('1130', 'Bank - Savings Account',             'ASSET',     'DEBIT',  @coa_current_assets,    3, 0, 'Company savings / reserve fund',                     1, 1, 1, NOW(), 1, NOW()),
('1140', 'Accounts Receivable',                'ASSET',     'DEBIT',  @coa_current_assets,    3, 0, 'Amounts owed by customers',                          1, 1, 1, NOW(), 1, NOW()),
('1150', 'Inventory',                          'ASSET',     'DEBIT',  @coa_current_assets,    3, 0, 'Goods held for sale (stock on hand)',                 1, 1, 1, NOW(), 1, NOW()),
('1160', 'Prepaid Expenses',                   'ASSET',     'DEBIT',  @coa_current_assets,    3, 0, 'Expenses paid in advance (e.g. prepaid rent)',        1, 1, 1, NOW(), 1, NOW()),
-- Fixed Assets (parent: 1200)
('1210', 'Land & Building',                    'ASSET',     'DEBIT',  @coa_fixed_assets,      3, 0, 'Company-owned land and building',                    1, 1, 1, NOW(), 1, NOW()),
('1220', 'Equipment',                          'ASSET',     'DEBIT',  @coa_fixed_assets,      3, 0, 'Machinery and equipment',                            1, 1, 1, NOW(), 1, NOW()),
('1230', 'Vehicles',                           'ASSET',     'DEBIT',  @coa_fixed_assets,      3, 0, 'Company vehicles',                                   1, 1, 1, NOW(), 1, NOW()),
('1240', 'Accumulated Depreciation - Equip',   'ASSET',     'CREDIT', @coa_fixed_assets,      3, 0, 'Contra-asset: accumulated depreciation on equipment', 1, 1, 1, NOW(), 1, NOW()),
('1250', 'Accumulated Depreciation - Vehicle', 'ASSET',     'CREDIT', @coa_fixed_assets,      3, 0, 'Contra-asset: accumulated depreciation on vehicles',  1, 1, 1, NOW(), 1, NOW()),
-- Short-term Liabilities (parent: 2100)
('2110', 'Accounts Payable',                   'LIABILITY', 'CREDIT', @coa_short_liab,        3, 0, 'Amounts owed to suppliers',                          1, 1, 1, NOW(), 1, NOW()),
('2120', 'Short-term Loan',                    'LIABILITY', 'CREDIT', @coa_short_liab,        3, 0, 'Bank loans due within 1 year',                       1, 1, 1, NOW(), 1, NOW()),
('2130', 'Tax Payable',                        'LIABILITY', 'CREDIT', @coa_short_liab,        3, 0, 'VAT and income tax payable',                         1, 1, 1, NOW(), 1, NOW()),
('2140', 'Accrued Liabilities',                'LIABILITY', 'CREDIT', @coa_short_liab,        3, 0, 'Expenses incurred but not yet paid',                 1, 1, 1, NOW(), 1, NOW()),
-- Long-term Liabilities (parent: 2200)
('2210', 'Long-term Loan',                     'LIABILITY', 'CREDIT', @coa_long_liab,         3, 0, 'Bank loans due beyond 1 year',                       1, 1, 1, NOW(), 1, NOW()),
-- Equity (parents: 3100, 3200)
('3110', 'Common Stock',                       'EQUITY',    'CREDIT', @coa_paid_in_capital,   3, 0, 'Ordinary shares issued to shareholders',             1, 1, 1, NOW(), 1, NOW()),
('3120', 'Additional Paid-in Capital',         'EQUITY',    'CREDIT', @coa_paid_in_capital,   3, 0, 'Share premium above par value',                      1, 1, 1, NOW(), 1, NOW()),
('3210', 'Retained Earnings',                  'EQUITY',    'CREDIT', @coa_retained_earnings, 3, 0, 'Cumulative net profit retained in company',          1, 1, 1, NOW(), 1, NOW()),
('3220', 'Current Year Profit/Loss',           'EQUITY',    'CREDIT', @coa_retained_earnings, 3, 0, 'Net income/loss for current fiscal year',            1, 1, 1, NOW(), 1, NOW()),
-- Sales Revenue (parent: 4100)
('4110', 'Product Sales',                      'REVENUE',   'CREDIT', @coa_sales_revenue,     3, 0, 'Revenue from sales of goods',                        1, 1, 1, NOW(), 1, NOW()),
('4120', 'Sales Discount',                     'REVENUE',   'DEBIT',  @coa_sales_revenue,     3, 0, 'Contra-revenue: discounts granted to customers',     1, 1, 1, NOW(), 1, NOW()),
('4130', 'Sales Return',                       'REVENUE',   'DEBIT',  @coa_sales_revenue,     3, 0, 'Contra-revenue: goods returned by customers',        1, 1, 1, NOW(), 1, NOW()),
-- Service Revenue (parent: 4200)
('4210', 'Service Revenue',                    'REVENUE',   'CREDIT', @coa_service_revenue,   3, 0, 'Revenue from professional services rendered',        1, 1, 1, NOW(), 1, NOW()),
-- COGS (parent: 5100)
('5110', 'COGS - Material',                    'EXPENSE',   'DEBIT',  @coa_cogs,              3, 0, 'Direct material cost of goods sold',                 1, 1, 1, NOW(), 1, NOW()),
('5120', 'COGS - Labor',                       'EXPENSE',   'DEBIT',  @coa_cogs,              3, 0, 'Direct labor cost of goods sold',                    1, 1, 1, NOW(), 1, NOW()),
-- Operating Expenses (parent: 5200)
('5210', 'Salaries & Wages',                   'EXPENSE',   'DEBIT',  @coa_opex,              3, 0, 'Employee salaries and wages',                        1, 1, 1, NOW(), 1, NOW()),
('5220', 'Utilities',                          'EXPENSE',   'DEBIT',  @coa_opex,              3, 0, 'Electricity, water, internet expenses',              1, 1, 1, NOW(), 1, NOW()),
('5230', 'Depreciation Expense',               'EXPENSE',   'DEBIT',  @coa_opex,              3, 0, 'Periodic depreciation of fixed assets',              1, 1, 1, NOW(), 1, NOW()),
('5240', 'Office Supplies',                    'EXPENSE',   'DEBIT',  @coa_opex,              3, 0, 'Consumable office materials',                        1, 1, 1, NOW(), 1, NOW()),
('5250', 'Rent Expense',                       'EXPENSE',   'DEBIT',  @coa_opex,              3, 0, 'Monthly office/warehouse rent',                      1, 1, 1, NOW(), 1, NOW()),
('5260', 'Transportation & Logistics',         'EXPENSE',   'DEBIT',  @coa_opex,              3, 0, 'Freight, delivery, and logistics costs',             1, 1, 1, NOW(), 1, NOW()),
('5270', 'Marketing & Advertising',            'EXPENSE',   'DEBIT',  @coa_opex,              3, 0, 'Promotions, ads, and marketing campaigns',           1, 1, 1, NOW(), 1, NOW()),
('5280', 'Tax Expense',                        'EXPENSE',   'DEBIT',  @coa_opex,              3, 0, 'Income tax and other tax charges',                   1, 1, 1, NOW(), 1, NOW());

-- ============================================================
-- GL VERIFICATION QUERIES (run manually to confirm)
-- ============================================================
-- SELECT COUNT(*) FROM acc_fiscal_years;         -- Expected: 2
-- SELECT COUNT(*) FROM acc_accounting_periods;   -- Expected: 24
-- SELECT COUNT(*) FROM acc_chart_of_accounts;    -- Expected: 42 (5+11+26)
-- SELECT level, COUNT(*) FROM acc_chart_of_accounts GROUP BY level ORDER BY level;
--   Expected: level 1 = 5, level 2 = 11, level 3 = 26
-- SELECT code, name, level, is_header FROM acc_chart_of_accounts ORDER BY level, code;

