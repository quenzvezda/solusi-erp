-- V9000: E2E Seed Data
-- Runs after all schema migrations. Sets up data needed for E2E tests.
-- Convention: V9000+ reserved for E2E-only seed data.
-- This file mirrors selected dev seeders (D010 + D011 role permissions, D020
-- parties, D030 users) plus extra transactional master data needed by the PR
-- approval E2E flow (facilities, products, supplier price list).

-- Ensure admin user can login without password change prompt
-- (SystemInitializer sets password_change_required=true, we override for E2E)
UPDATE users SET password_change_required = false WHERE username = 'admin';

-- ====== ROLES (mirror D010) ======
-- Production migrations only seed ROLE_ADMIN. Dev seeder D010 adds the rest.
INSERT INTO roles (name, description, created_by_user_id, created_date, version) VALUES
('ROLE_APPROVER',  'Approver — dapat mereview dan menyetujui dokumen', 1, NOW(), 1),
('ROLE_WAREHOUSE', 'Warehouse Operator — mengelola stok dan gudang',   1, NOW(), 1),
('ROLE_EMPLOYEE',  'Karyawan — akses dasar untuk membaca berita dan dashboard', 1, NOW(), 1);

SET @role_admin_id    = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');
SET @role_approver_id = (SELECT id FROM roles WHERE name = 'ROLE_APPROVER');
SET @role_warehouse_id = (SELECT id FROM roles WHERE name = 'ROLE_WAREHOUSE');
SET @role_employee_id = (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE');

-- ROLE_APPROVER: Dashboard + News + Approval + Party Lookup (mirror D010)
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_approver_id, id FROM permissions WHERE name IN (
    'DASHBOARD_READ',
    'NEWS_READ', 'NEWS_CREATE', 'NEWS_UPDATE',
    'APPROVAL_READ', 'APPROVAL_PROCESS',
    'LOOKUP_PARTY',
    'DASHBOARD_NEWS', 'DASHBOARD_APPROVAL'
);

-- ROLE_WAREHOUSE: Inventory management (mirror D010)
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

-- ROLE_EMPLOYEE: Dashboard + News read-only (mirror D010)
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_employee_id, id FROM permissions WHERE name IN (
    'DASHBOARD_READ',
    'NEWS_READ',
    'DASHBOARD_NEWS'
);

-- ====== ROLE PERMISSIONS — PROCUREMENT (mirror D011) ======

-- ROLE_APPROVER: PR/PO/SPL read+update (cancel) + lookup permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_approver_id, id FROM permissions WHERE name IN (
    'PR_READ', 'PR_UPDATE',
    'LOOKUP_PR',
    'PO_READ', 'PO_UPDATE',
    'LOOKUP_PO',
    'SPL_READ',
    'LOOKUP_SUPPLIER-PRICE-LIST',
    'LOOKUP_INVENTORY',
    'LOOKUP_BRAND',
    'LOOKUP_PRODUCT-CATEGORY',
    'LOOKUP_FACILITY',
    'LOOKUP_UOM-CONVERSION'
);

-- ROLE_EMPLOYEE: Full PR requester lifecycle + supporting lookups
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_employee_id, id FROM permissions WHERE name IN (
    'PR_READ', 'PR_CREATE', 'PR_UPDATE', 'PR_DELETE', 'PR_SUBMIT',
    'LOOKUP_PR',
    'SPL_READ',
    'LOOKUP_SUPPLIER-PRICE-LIST',
    'LOOKUP_INVENTORY',
    'LOOKUP_BRAND',
    'LOOKUP_PRODUCT-CATEGORY',
    'LOOKUP_FACILITY',
    'LOOKUP_UOM-CONVERSION',
    'LOOKUP_PARTY'
);

-- ====== PARTY ROLE TYPE: APPROVER (mirror D020) ======
INSERT INTO party_role_types (code, name, created_by_user_id, created_date, version)
SELECT 'APPROVER', 'Approver', 1, NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM party_role_types WHERE code = 'APPROVER');

SET @prt_internal  = (SELECT id FROM party_role_types WHERE code = 'INTERNAL');
SET @prt_supplier  = (SELECT id FROM party_role_types WHERE code = 'SUPPLIER');
SET @prt_employee  = (SELECT id FROM party_role_types WHERE code = 'EMPLOYEE');
SET @prt_warehouse = (SELECT id FROM party_role_types WHERE code = 'WAREHOUSE_OPERATOR');
SET @prt_approver  = (SELECT id FROM party_role_types WHERE code = 'APPROVER');

-- ====== PARTIES (minimal mirror of D020) ======
-- Only the parties needed by D030 users + one supplier for PR flow.

INSERT INTO parties (code, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version) VALUES
('BP-DEV-APR01',  'Budi Santoso',     'PERSON',       1, 'budi.santoso@solusierp.com',   '081234567891', 1, NOW(), 1, NOW(), 1),
('BP-DEV-APR02',  'Siti Rahayu',      'PERSON',       1, 'siti.rahayu@solusierp.com',    '081234567892', 1, NOW(), 1, NOW(), 1),
('BP-DEV-WH01',   'Ahmad Fadli',      'PERSON',       1, 'ahmad.fadli@solusierp.com',    '081234567893', 1, NOW(), 1, NOW(), 1),
('BP-DEV-EMP01',  'Dewi Lestari',     'PERSON',       1, 'dewi.lestari@solusierp.com',   '081234567894', 1, NOW(), 1, NOW(), 1),
('BP-DEV-SUP01',  'PT. Sumber Makmur','ORGANIZATION', 1, 'sales@sumbermakmur.co.id',     '021-5554003',  1, NOW(), 1, NOW(), 1);

SET @p_apr1 = (SELECT id FROM parties WHERE code = 'BP-DEV-APR01');
SET @p_apr2 = (SELECT id FROM parties WHERE code = 'BP-DEV-APR02');
SET @p_wh1  = (SELECT id FROM parties WHERE code = 'BP-DEV-WH01');
SET @p_emp1 = (SELECT id FROM parties WHERE code = 'BP-DEV-EMP01');
SET @p_sup1 = (SELECT id FROM parties WHERE code = 'BP-DEV-SUP01');

INSERT INTO party_roles (party_id, role_type_id) VALUES
(@p_apr1, @prt_approver),  (@p_apr1, @prt_employee),
(@p_apr2, @prt_approver),  (@p_apr2, @prt_employee),
(@p_wh1,  @prt_warehouse), (@p_wh1,  @prt_employee),
(@p_emp1, @prt_employee),
(@p_sup1, @prt_supplier);

-- ====== USERS (mirror D030) ======
-- BCrypt hash for "admin123"
SET @pwd = '$2b$10$kJlPG9rZovRB57u0POwoQujVA0EK4kI6qWpUZJD/O8fa5WxfyAGca';

INSERT INTO users (username, password, email, enabled, password_change_required, role_id, party_id, created_by_user_id, created_date, version) VALUES
('approver1',  @pwd, 'budi.santoso@solusierp.com', 1, 0, @role_approver_id,  @p_apr1, 1, NOW(), 1),
('approver2',  @pwd, 'siti.rahayu@solusierp.com',  1, 0, @role_approver_id,  @p_apr2, 1, NOW(), 1),
('warehouse1', @pwd, 'ahmad.fadli@solusierp.com',  1, 0, @role_warehouse_id, @p_wh1,  1, NOW(), 1),
('employee1',  @pwd, 'dewi.lestari@solusierp.com', 1, 0, @role_employee_id,  @p_emp1, 1, NOW(), 1);

-- ====== E2E MASTER DATA (id range 9001+) ======

-- Unit of Measures
INSERT INTO unit_of_measures (id, code, name, type, created_by_user_id, created_date, version) VALUES
(9001, 'E2E-PCS', 'E2E Piece',      'UNIT',   1, NOW(), 1),
(9002, 'E2E-KG',  'E2E Kilogram',   'WEIGHT', 1, NOW(), 1),
(9003, 'E2E-CM',  'E2E Centimeter', 'LENGTH', 1, NOW(), 1);

-- Product Categories
INSERT INTO product_categories (id, code, name, type, note, created_by_user_id, created_date, version) VALUES
(9001, 'E2E-CAT-STOCK', 'E2E Category Stock',   'STOCK',   'E2E test category for stock items', 1, NOW(), 1),
(9002, 'E2E-CAT-SVC',   'E2E Category Service', 'SERVICE', 'E2E test category for services',    1, NOW(), 1);

-- Brands
INSERT INTO brands (id, code, name, created_by_user_id, created_date, version) VALUES
(9001, 'E2E-BRAND-A', 'E2E Brand Alpha', 1, NOW(), 1),
(9002, 'E2E-BRAND-B', 'E2E Brand Beta',  1, NOW(), 1);

-- ====== E2E TRANSACTIONAL MASTER (id range 9100+ for PR flow) ======

-- Facility (warehouse) — required by PR header.facility_id
INSERT INTO inv_facilities (id, code, name, owner_id, is_active, created_by_user_id, created_date, version) VALUES
(9101, 'E2E-WH01', 'E2E Main Warehouse', @p_sup1, 1, 1, NOW(), 1);

-- Products — required by PR line.product_id
INSERT INTO products (code, name, category_id, uom_id, brand_id, is_active, created_by_user_id, created_date, version) VALUES
('E2E-PRD-LAPTOP', 'E2E Laptop 14 inch',  9001, 9001, 9001, TRUE, 1, NOW(), 1),
('E2E-PRD-CHAIR',  'E2E Office Chair',    9001, 9001, 9002, TRUE, 1, NOW(), 1);

SET @prd_laptop = (SELECT id FROM products WHERE code = 'E2E-PRD-LAPTOP');
SET @prd_chair  = (SELECT id FROM products WHERE code = 'E2E-PRD-CHAIR');

SET @cur_idr = (SELECT id FROM master_currencies WHERE alias = 'IDR');

-- Supplier Price List (active row, used by Scenario F autofill check)
INSERT INTO pur_supplier_price_lists
    (code, supplier_id, product_id, uom_id, currency_id, unit_price, min_quantity, effective_from, effective_to, is_active, version, created_by_user_id, created_date)
VALUES
    ('E2E-SPL-001', @p_sup1, @prd_laptop, 9001, @cur_idr, 8500000.0000, 1, '2026-01-01', NULL, TRUE, 1, 1, NOW());
