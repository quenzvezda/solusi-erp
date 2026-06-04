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
    'STOCK-ADJUSTMENT_READ', 'STOCK-ADJUSTMENT_CREATE', 'STOCK-ADJUSTMENT_UPDATE', 'STOCK-ADJUSTMENT_DELETE', 'STOCK-ADJUSTMENT_PROCESS',
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

-- ====== INVENTORY HIERARCHY (Stock Adjustment scenarios) ======
-- Grid 9101 + Container 9101 in facility 9101 — required by SA line.container_id
-- (Stock Adjustment positive flow creates balance + valuation layer; no pre-seed needed.)
INSERT INTO inv_grids (id, facility_id, code, name, is_active, version, created_by_user_id, created_date) VALUES
(9101, 9101, 'E2E-GRD-A', 'E2E Grid A', 1, 1, 1, NOW());

INSERT INTO inv_containers (id, grid_id, code, name, is_active, version, created_by_user_id, created_date) VALUES
(9101, 9101, 'E2E-CTN-A1', 'E2E Container A1', 1, 1, 1, NOW());

-- ====== E2E PURCHASE ORDER SEED (Sprint: PO E2E) ======
-- Tax (id 9001) — PPN 0% / Non Tax. Required by PO header which mandates a
-- tax selection even for non-taxable transactions.
INSERT INTO taxes (id, code, name, rate, calculation_mode, is_subtract, is_active, created_by_user_id, created_date, version) VALUES
(9001, 'E2E-TAX-0', 'E2E PPN 0% Non Tax', 0.0000, 'EXCLUSIVE', FALSE, TRUE, 1, NOW(), 1);

-- Grant ROLE_WAREHOUSE the PO + supporting lookup permissions needed to
-- exercise the PO STANDARD lifecycle E2E (create from PR, submit, send,
-- cancel, delete).
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_warehouse_id, id FROM permissions WHERE name IN (
    'PO_READ', 'PO_CREATE', 'PO_UPDATE', 'PO_DELETE', 'PO_SUBMIT', 'PO_SEND',
    'LOOKUP_PR', 'LOOKUP_PO',
    'LOOKUP_TAX',
    'LOOKUP_PARTY'
);

-- Approved PR (id 9301) used as the source for STANDARD PO E2E.
-- requester_id points to employee1's user (deterministic within test scope).
SET @u_emp1 = (SELECT id FROM users WHERE username = 'employee1');

INSERT INTO pur_purchase_requisitions
    (id, code, request_date, requester_id, facility_id, department, priority, status,
     suggested_supplier_id, currency_id, note, is_active, version,
     created_by_user_id, created_date)
VALUES
    (9301, 'E2E-PR-9301', '2026-05-19', @u_emp1, 9101, 'IT', 'HIGH', 'APPROVED',
     @p_sup1, @cur_idr, 'E2E seed for PO STANDARD flow', TRUE, 1,
     1, NOW());

-- PR lines (ids 9301, 9302). converted_po_line_id is NULL so the PR-line
-- selector still treats remaining qty as fully open.
INSERT INTO pur_purchase_requisition_lines
    (id, header_id, product_id, quantity, uom_id, required_date, estimated_unit_price,
     converted_po_line_id, note, version, created_by_user_id, created_date)
VALUES
    (9301, 9301, @prd_laptop, 999.0000, 9001, '2026-05-30', 8500000.0000, NULL, 'E2E line laptop', 1, 1, NOW()),
    (9302, 9301, @prd_chair,  999.0000, 9001, '2026-05-30', 1500000.0000, NULL, 'E2E line chair',  1, 1, NOW());

-- ====== E2E ACCOUNTING FOUNDATION FOR INVENTORY POSTING ======
-- Goods Receipt completion enforces an open accounting period and posts a balanced journal.
INSERT INTO acc_fiscal_years
    (id, code, name, start_date, end_date, is_active, version, created_by_user_id, created_date)
VALUES
    (9401, 'E2E-FY-2026', 'E2E Fiscal Year 2026', '2026-01-01', '2026-12-31', TRUE, 1, 1, NOW());

INSERT INTO acc_accounting_periods
    (id, code, name, fiscal_year_id, period_number, start_date, end_date, status,
     version, created_by_user_id, created_date)
VALUES
    (9401, 'E2E-2026-05', 'E2E May 2026', 9401, 5, '2026-05-01', '2026-05-31', 'OPEN',
     1, 1, NOW());

INSERT INTO acc_chart_of_accounts
    (id, code, name, account_type, normal_balance, parent_id, level, is_header, note,
     is_active, version, created_by_user_id, created_date)
VALUES
    (9401, '1130', 'E2E Inventory', 'ASSET', 'DEBIT', NULL, 1, FALSE, 'E2E inventory account', TRUE, 1, 1, NOW()),
    (9402, '2110', 'E2E Goods Receipt Accrual', 'LIABILITY', 'CREDIT', NULL, 1, FALSE, 'E2E GR accrual account', TRUE, 1, 1, NOW()),
    (9403, '2120', 'E2E Accounts Payable', 'LIABILITY', 'CREDIT', NULL, 1, FALSE, 'E2E AP account', TRUE, 1, 1, NOW()),
    (9404, '1140', 'E2E Input VAT', 'ASSET', 'DEBIT', NULL, 1, FALSE, 'E2E input VAT account', TRUE, 1, 1, NOW()),
    (9405, '1120', 'E2E Bank', 'ASSET', 'DEBIT', NULL, 1, FALSE, 'E2E bank account', TRUE, 1, 1, NOW()),
    (9406, '5140', 'E2E FX Loss', 'EXPENSE', 'DEBIT', NULL, 1, FALSE, 'E2E FX loss account', TRUE, 1, 1, NOW()),
    (9407, '4240', 'E2E FX Gain', 'REVENUE', 'CREDIT', NULL, 1, FALSE, 'E2E FX gain account', TRUE, 1, 1, NOW());

INSERT INTO acc_accounting_schemas
    (id, event_type, description, is_active, version, created_by_user_id, created_date)
VALUES
    (9401, 'GOODS_RECEIPT', 'E2E goods receipt posting schema', TRUE, 1, 1, NOW()),
    (9402, 'VENDOR_BILL', 'E2E vendor bill posting schema', TRUE, 1, 1, NOW());

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
VALUES
    (9401, 'GR_INVENTORY_AMT', 9401, 'DEBIT'),
    (9401, 'GR_GRAND_TOTAL', 9402, 'CREDIT'),
    (9402, 'VB_GRIR_CLEARING_AMT', 9402, 'DEBIT'),
    (9402, 'VB_TAX_AMT', 9404, 'DEBIT'),
    (9402, 'VB_AP_TOTAL', 9403, 'CREDIT');

-- V62 runs before E2E-only COA rows exist, so refresh VENDOR_PAYMENT schema lines here.
SET @vp_schema_id = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'VENDOR_PAYMENT' AND is_active = TRUE LIMIT 1);

INSERT INTO acc_accounting_schemas
    (event_type, description, is_active, version, created_by_user_id, created_date)
SELECT 'VENDOR_PAYMENT', 'E2E vendor payment posting schema', TRUE, 1, 1, NOW()
WHERE @vp_schema_id IS NULL;

SET @vp_schema_id = COALESCE(@vp_schema_id, LAST_INSERT_ID());

DELETE FROM acc_schema_lines WHERE schema_id = @vp_schema_id;

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
VALUES
    (@vp_schema_id, 'VP_AP_AMT', 9403, 'DEBIT'),
    (@vp_schema_id, 'VP_BANK_OUT_AMT', 9405, 'CREDIT'),
    (@vp_schema_id, 'VP_FX_LOSS_AMT', 9406, 'DEBIT'),
    (@vp_schema_id, 'VP_FX_GAIN_AMT', 9407, 'CREDIT');

SET @city_e2e = (SELECT id FROM geographics WHERE type = 'CITY_MUNICIPALITY' ORDER BY id LIMIT 1);

INSERT INTO bank_accounts
    (id, code, bank_name, branch, city_id, party_id, account_name, account_no, account_type,
     note, is_active, created_by_user_id, created_date, updated_by_user_id, updated_date, version, currency_id, coa_id)
VALUES
    (9501, 'E2E-BA-001', 'E2E Bank', 'E2E Main Branch', @city_e2e, @p_sup1,
     'E2E Operational', '1234567890', 'BANK_TRANSFER', 'E2E bank account for vendor payment flow',
     TRUE, 1, NOW(), 1, NOW(), 1, @cur_idr, 9405);

-- ====== E2E GOODS RECEIPT SEED ======
-- Sent PO (id 9201) used as the source for Goods Receipt E2E.
-- GR source resolution requires a PO status that canReceive(): SENT or PARTIALLY_RECEIVED.
INSERT INTO pur_purchase_orders
    (id, code, order_date, expected_date, supplier_id, facility_id, currency_id, exchange_rate,
     subtotal, tax_amount, total_amount, status, payment_term_days, pr_id, po_type, note,
     is_active, version, created_by_user_id, created_date,
     tax_id, tax_code, tax_name, tax_rate, tax_calculation_mode)
VALUES
    (9201, 'E2E-PO-9201', '2026-05-19', '2026-05-30', @p_sup1, 9101, @cur_idr, 1.000000,
     176000000.0000, 0.0000, 176000000.0000, 'SENT', 30, NULL, 'DIRECT',
     'E2E seed for Goods Receipt flow', TRUE, 1, 1, NOW(),
     9001, 'E2E-TAX-0', 'E2E PPN 0% Non Tax', 0.0000, 'EXCLUSIVE');

-- PO lines (ids 9201, 9202). received_quantity is zero so all qty remains open.
INSERT INTO pur_purchase_order_lines
    (id, header_id, product_id, quantity, received_quantity, uom_id, unit_price,
     tax_rate, line_subtotal, line_tax, line_total, pr_line_id, note,
     version, created_by_user_id, created_date)
VALUES
    (9201, 9201, @prd_laptop, 20.0000, 0.0000, 9001, 8500000.0000,
     0.0000, 170000000.0000, 0.0000, 170000000.0000, NULL, 'E2E GR line laptop',
     1, 1, NOW()),
    (9202, 9201, @prd_chair, 4.0000, 0.0000, 9001, 1500000.0000,
     0.0000, 6000000.0000, 0.0000, 6000000.0000, NULL, 'E2E GR line chair',
     1, 1, NOW());

-- Grant ROLE_WAREHOUSE the Goods Receipt lifecycle permissions used by the GR E2E spec.
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_warehouse_id, id FROM permissions WHERE name IN (
    'GOODS-RECEIPT_READ', 'GOODS-RECEIPT_CREATE', 'GOODS-RECEIPT_UPDATE',
    'GOODS-RECEIPT_DELETE', 'GOODS-RECEIPT_COMPLETE'
);

-- ====== E2E PURCHASE RETURN SEED ======
-- ROLE_EMPLOYEE owns the requester lifecycle. ROLE_APPROVER needs the detail
-- page to process the generic approval request. GOODS-ISSUE_READ lets the
-- requester follow the generated GI link after confirm. JOURNAL-ENTRY_READ
-- lets the requester verify the generated Purchase Return journal.
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_employee_id, id FROM permissions WHERE name IN (
    'PURCHASE-RETURN_READ', 'PURCHASE-RETURN_CREATE', 'PURCHASE-RETURN_UPDATE',
    'PURCHASE-RETURN_SUBMIT', 'PURCHASE-RETURN_CONFIRM', 'PURCHASE-RETURN_CANCEL',
    'GOODS-ISSUE_READ', 'JOURNAL-ENTRY_READ'
);
INSERT INTO role_permissions (role_id, permission_id)
SELECT @role_approver_id, id FROM permissions WHERE name IN (
    'PURCHASE-RETURN_READ'
);

-- Second container represents stock moved after its original receipt.
INSERT INTO inv_containers (id, grid_id, code, name, is_active, version, created_by_user_id, created_date) VALUES
(9102, 9101, 'E2E-CTN-A2', 'E2E Container A2 Moved', 1, 1, 1, NOW());

INSERT INTO products
    (code, name, category_id, uom_id, brand_id, is_active, is_serialized,
     created_by_user_id, created_date, version)
VALUES
    ('E2E-PRD-SERIAL', 'E2E Serialized Scanner', 9001, 9001, 9001, TRUE, TRUE, 1, NOW(), 1);
SET @prd_serial = (SELECT id FROM products WHERE code = 'E2E-PRD-SERIAL');

-- Separate sent PO and completed GR keep the Purchase Return flow independent
-- from Goods Receipt scenarios that create their own transactional rows.
INSERT INTO pur_purchase_orders
    (id, code, order_date, expected_date, supplier_id, facility_id, currency_id, exchange_rate,
     subtotal, tax_amount, total_amount, status, payment_term_days, pr_id, po_type, note,
     is_active, version, created_by_user_id, created_date,
     tax_id, tax_code, tax_name, tax_rate, tax_calculation_mode)
VALUES
    (9202, 'E2E-PO-RETURN-9202', '2026-05-20', '2026-05-25', @p_sup1, 9101, @cur_idr, 1.000000,
     43700000.0000, 0.0000, 43700000.0000, 'SENT', 30, NULL, 'DIRECT',
     'E2E seed for Purchase Return flow', TRUE, 1, 1, NOW(),
     9001, 'E2E-TAX-0', 'E2E PPN 0% Non Tax', 0.0000, 'EXCLUSIVE');
INSERT INTO pur_purchase_order_lines
    (id, header_id, product_id, quantity, received_quantity, uom_id, unit_price,
     tax_rate, line_subtotal, line_tax, line_total, pr_line_id, note,
     version, created_by_user_id, created_date)
VALUES
    (9211, 9202, @prd_laptop, 5.0000, 5.0000, 9001, 8500000.0000,
     0.0000, 42500000.0000, 0.0000, 42500000.0000, NULL, 'E2E return laptop', 1, 1, NOW()),
    (9212, 9202, @prd_serial, 1.0000, 1.0000, 9001, 1200000.0000,
     0.0000, 1200000.0000, 0.0000, 1200000.0000, NULL, 'E2E return serialized scanner', 1, 1, NOW());

INSERT INTO pur_goods_receipts
    (id, code, receipt_date, reference_type, reference_id, supplier_id, facility_id, currency_id, exchange_rate,
     status, note, version, created_by_user_id, created_date)
VALUES
    (9601, 'E2E-GR-RETURN-9601', '2026-05-25', 'PURCHASE_ORDER', 9202, @p_sup1, 9101, @cur_idr, 1.000000,
     'COMPLETED', 'E2E completed GR for Purchase Return', 1, 1, NOW());
INSERT INTO pur_goods_receipt_lines
    (id, header_id, reference_line_id, product_id, quantity_received, uom_id, container_id,
     base_quantity, inventory_amount, tax_base_amount, tax_amount, gr_ir_amount, serial_number,
     is_serialized, version, created_by_user_id, created_date)
VALUES
    (9601, 9601, 9211, @prd_laptop, 5.0000, 9001, 9101,
     5.0000, 42500000.0000, 42500000.0000, 0.0000, 42500000.0000, NULL,
     FALSE, 1, 1, NOW()),
    (9602, 9601, 9212, @prd_serial, 1.0000, 9001, 9101,
     1.0000, 1200000.0000, 1200000.0000, 0.0000, 1200000.0000, 'E2E-SER-MOVED-001',
     TRUE, 1, 1, NOW());

-- The non-serialized GR line is currently split between two containers.
-- The serial was received in A1 but its current layer and balance live in A2.
INSERT INTO inv_stock_balances
    (product_id, container_id, serial_number, quantity, reserved_quantity, in_transit_quantity,
     version, created_by_user_id, created_date)
VALUES
    (@prd_laptop, 9101, NULL, 3.0000, 0.0000, 0.0000, 1, 1, NOW()),
    (@prd_laptop, 9102, NULL, 2.0000, 0.0000, 0.0000, 1, 1, NOW()),
    (@prd_serial, 9102, 'E2E-SER-MOVED-001', 1.0000, 0.0000, 0.0000, 1, 1, NOW());
INSERT INTO inv_valuation_layers
    (product_id, container_id, serial_number, initial_quantity, remaining_quantity,
     unit_cost_currency_id, unit_cost_exchange_rate, unit_cost_amount_original, unit_cost_amount_local,
     version, created_by_user_id, created_date, reference_type, reference_id, reference_line_id)
VALUES
    (@prd_laptop, 9101, NULL, 3.0000, 3.0000, @cur_idr, 1.000000, 8500000.0000, 8500000.0000,
     1, 1, NOW(), 'GOODS_RECEIPT', 9601, 9601),
    (@prd_laptop, 9102, NULL, 2.0000, 2.0000, @cur_idr, 1.000000, 8500000.0000, 8500000.0000,
     1, 1, NOW(), 'GOODS_RECEIPT', 9601, 9601),
    (@prd_serial, 9102, 'E2E-SER-MOVED-001', 1.0000, 1.0000, @cur_idr, 1.000000, 1200000.0000, 1200000.0000,
     1, 1, NOW(), 'GOODS_RECEIPT', 9601, 9602);

-- The create page defaults returnDate to the current date, so keep June open
-- in addition to the May period used by older inventory specs.
INSERT INTO acc_accounting_periods
    (id, code, name, fiscal_year_id, period_number, start_date, end_date, status,
     version, created_by_user_id, created_date)
VALUES
    (9402, 'E2E-2026-06', 'E2E June 2026', 9401, 6, '2026-06-01', '2026-06-30', 'OPEN',
     1, 1, NOW());
INSERT INTO acc_chart_of_accounts
    (id, code, name, account_type, normal_balance, parent_id, level, is_header, note,
     is_active, version, created_by_user_id, created_date)
VALUES
    (9408, '5150', 'E2E Goods Issue Expense', 'EXPENSE', 'DEBIT', NULL, 1, FALSE,
     'E2E generic GI counterpart account', TRUE, 1, 1, NOW());
INSERT INTO acc_accounting_schemas
    (id, event_type, description, is_active, version, created_by_user_id, created_date)
VALUES
    (9403, 'GOODS_ISSUE', 'E2E generic goods issue posting schema', TRUE, 1, 1, NOW());
INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
VALUES
    (9403, 'GI_COGS_AMT', 9408, 'DEBIT'),
    (9403, 'GI_INVENTORY_AMT', 9401, 'CREDIT');

-- V71 runs before E2E-only COA rows exist, so refresh PURCHASE_RETURN schema lines here.
SET @prt_schema_id = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'PURCHASE_RETURN' AND is_active = TRUE LIMIT 1);

INSERT INTO acc_accounting_schemas
    (event_type, description, is_active, version, created_by_user_id, created_date)
SELECT 'PURCHASE_RETURN', 'E2E purchase return posting schema', TRUE, 1, 1, NOW()
WHERE @prt_schema_id IS NULL;

SET @prt_schema_id = COALESCE(@prt_schema_id, LAST_INSERT_ID());

DELETE FROM acc_schema_lines WHERE schema_id = @prt_schema_id;

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
VALUES
    (@prt_schema_id, 'PR_GRIR_CLEARING_AMT', 9402, 'DEBIT'),
    (@prt_schema_id, 'PR_INVENTORY_AMT', 9401, 'CREDIT');
