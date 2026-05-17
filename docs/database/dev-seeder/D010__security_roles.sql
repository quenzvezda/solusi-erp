-- Development seeder security roles and permissions.

-- BCrypt hash for "admin123"
SET @pwd = '$2b$10$kJlPG9rZovRB57u0POwoQujVA0EK4kI6qWpUZJD/O8fa5WxfyAGca';

INSERT INTO roles (name, description, created_by_user_id, created_date, version) VALUES
('ROLE_APPROVER', 'Approver — dapat mereview dan menyetujui dokumen', 1, NOW(), 1),
('ROLE_WAREHOUSE', 'Warehouse Operator — mengelola stok dan gudang', 1, NOW(), 1),
('ROLE_EMPLOYEE', 'Karyawan — akses dasar untuk membaca berita dan dashboard', 1, NOW(), 1);

SET @role_admin_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');
SET @role_approver_id = (SELECT id FROM roles WHERE name = 'ROLE_APPROVER');
SET @role_warehouse_id = (SELECT id FROM roles WHERE name = 'ROLE_WAREHOUSE');
SET @role_employee_id = (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE');

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
