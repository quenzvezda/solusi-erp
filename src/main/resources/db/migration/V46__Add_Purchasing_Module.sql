-- V46: Purchasing Module — consolidated from V46 to V50

-- ============================================================
-- 0. ALTER: Add is_pkp to parties table
-- ============================================================
ALTER TABLE parties ADD COLUMN IF NOT EXISTS is_pkp BOOLEAN NOT NULL DEFAULT FALSE AFTER is_active;

-- ============================================================
-- 1. DDL: Supplier Price List
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_supplier_price_lists (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    code               VARCHAR(30)   NOT NULL,
    supplier_id        BIGINT        NOT NULL,
    product_id         BIGINT        NOT NULL,
    uom_id             BIGINT        NOT NULL,
    currency_id        BIGINT        NOT NULL,
    unit_price         DECIMAL(19,4) NOT NULL,
    min_quantity       DECIMAL(19,4) NOT NULL DEFAULT 1,
    effective_from     DATE          NOT NULL,
    effective_to       DATE          NULL,
    note               TEXT          NULL,
    is_active          BOOLEAN       NOT NULL DEFAULT TRUE,
    version            INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT        NULL,
    created_date       DATETIME      NULL,
    updated_by_user_id BIGINT        NULL,
    updated_date       DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_spl_code (code),
    UNIQUE KEY uk_spl_combo (supplier_id, product_id, uom_id, currency_id, effective_from),
    CONSTRAINT fk_spl_supplier     FOREIGN KEY (supplier_id)        REFERENCES parties(id),
    CONSTRAINT fk_spl_product      FOREIGN KEY (product_id)          REFERENCES products(id),
    CONSTRAINT fk_spl_uom          FOREIGN KEY (uom_id)              REFERENCES unit_of_measures(id),
    CONSTRAINT fk_spl_currency     FOREIGN KEY (currency_id)         REFERENCES master_currencies(id),
    CONSTRAINT fk_spl_created_by   FOREIGN KEY (created_by_user_id)  REFERENCES users(id),
    CONSTRAINT fk_spl_updated_by   FOREIGN KEY (updated_by_user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. DDL: Purchase Requisition (Header)
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_requisitions (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    code                 VARCHAR(30)   NOT NULL,
    request_date         DATE          NOT NULL,
    requester_id         BIGINT        NULL,
    facility_id          BIGINT        NULL,
    department           VARCHAR(50)   NULL,
    priority             VARCHAR(10)   NOT NULL COMMENT 'LOW | NORMAL | HIGH | URGENT',
    status               VARCHAR(20)   NOT NULL COMMENT 'DRAFT | SUBMITTED | APPROVED | CONVERTED | CANCELLED | REJECTED',
    suggested_supplier_id BIGINT       NULL,
    currency_id          BIGINT        NOT NULL,
    note                 TEXT          NULL,
    is_active            BOOLEAN       NOT NULL DEFAULT TRUE,
    version              INT           NOT NULL DEFAULT 0,
    created_by_user_id   BIGINT        NULL,
    created_date         DATETIME      NULL,
    updated_by_user_id   BIGINT        NULL,
    updated_date         DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pr_code (code),
    CONSTRAINT fk_pr_requester       FOREIGN KEY (requester_id)         REFERENCES users(id),
    CONSTRAINT fk_pr_facility        FOREIGN KEY (facility_id)          REFERENCES inv_facilities(id),
    CONSTRAINT fk_pr_suggested_supplier FOREIGN KEY (suggested_supplier_id) REFERENCES parties(id),
    CONSTRAINT fk_pr_currency       FOREIGN KEY (currency_id)          REFERENCES master_currencies(id),
    CONSTRAINT fk_pr_created_by     FOREIGN KEY (created_by_user_id)   REFERENCES users(id),
    CONSTRAINT fk_pr_updated_by     FOREIGN KEY (updated_by_user_id)   REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. DDL: Purchase Requisition Lines
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_requisition_lines (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    header_id             BIGINT        NOT NULL,
    product_id            BIGINT        NOT NULL,
    quantity              DECIMAL(19,4) NOT NULL,
    uom_id                BIGINT        NOT NULL,
    required_date         DATE          NULL,
    estimated_unit_price  DECIMAL(19,4) NULL,
    converted_po_line_id  BIGINT        NULL,
    note                  TEXT          NULL,
    version               INT           NOT NULL DEFAULT 0,
    created_by_user_id    BIGINT        NULL,
    created_date          DATETIME      NULL,
    updated_by_user_id    BIGINT        NULL,
    updated_date          DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_prl_header      FOREIGN KEY (header_id)            REFERENCES pur_purchase_requisitions(id),
    CONSTRAINT fk_prl_product     FOREIGN KEY (product_id)           REFERENCES products(id),
    CONSTRAINT fk_prl_uom         FOREIGN KEY (uom_id)               REFERENCES unit_of_measures(id),
    CONSTRAINT fk_prl_created_by  FOREIGN KEY (created_by_user_id)   REFERENCES users(id),
    CONSTRAINT fk_prl_updated_by  FOREIGN KEY (updated_by_user_id)   REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. DDL: Purchase Order (Header)
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_orders (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    code               VARCHAR(30)   NOT NULL,
    order_date         DATE          NOT NULL,
    expected_date      DATE          NULL,
    supplier_id        BIGINT        NOT NULL,
    facility_id        BIGINT        NULL,
    currency_id        BIGINT        NOT NULL,
    exchange_rate      DECIMAL(19,6) NOT NULL,
    subtotal           DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount         DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount       DECIMAL(19,4) NOT NULL DEFAULT 0,
    status             VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    payment_term_days  INT           NOT NULL DEFAULT 30,
    pr_id              BIGINT        NULL,
    po_type            VARCHAR(10)   NOT NULL DEFAULT 'DIRECT',
    note               TEXT          NULL,
    is_active          BOOLEAN       NOT NULL DEFAULT TRUE,
    version            INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT        NULL,
    created_date       DATETIME      NULL,
    updated_by_user_id BIGINT        NULL,
    updated_date       DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_po_code (code),
    CONSTRAINT fk_po_supplier     FOREIGN KEY (supplier_id)        REFERENCES parties(id),
    CONSTRAINT fk_po_facility     FOREIGN KEY (facility_id)        REFERENCES inv_facilities(id),
    CONSTRAINT fk_po_currency     FOREIGN KEY (currency_id)        REFERENCES master_currencies(id),
    CONSTRAINT fk_po_pr           FOREIGN KEY (pr_id)              REFERENCES pur_purchase_requisitions(id),
    CONSTRAINT fk_po_created_by   FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_po_updated_by   FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. DDL: Purchase Order Lines
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_order_lines (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    header_id          BIGINT        NOT NULL,
    product_id         BIGINT        NOT NULL,
    quantity           DECIMAL(19,4) NOT NULL,
    received_quantity  DECIMAL(19,4) NOT NULL DEFAULT 0,
    uom_id             BIGINT        NOT NULL,
    unit_price         DECIMAL(19,4) NOT NULL,
    tax_rate           DECIMAL(5,4)   NOT NULL DEFAULT 0,
    line_subtotal      DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_tax           DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_total         DECIMAL(19,4) NOT NULL DEFAULT 0,
    pr_line_id         BIGINT        NULL,
    note               TEXT          NULL,
    version            INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT        NULL,
    created_date       DATETIME      NULL,
    updated_by_user_id BIGINT        NULL,
    updated_date       DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pol_header      FOREIGN KEY (header_id)           REFERENCES pur_purchase_orders(id),
    CONSTRAINT fk_pol_product     FOREIGN KEY (product_id)          REFERENCES products(id),
    CONSTRAINT fk_pol_uom         FOREIGN KEY (uom_id)              REFERENCES unit_of_measures(id),
    CONSTRAINT fk_pol_pr_line     FOREIGN KEY (pr_line_id)          REFERENCES pur_purchase_requisition_lines(id),
    CONSTRAINT fk_pol_created_by  FOREIGN KEY (created_by_user_id)  REFERENCES users(id),
    CONSTRAINT fk_pol_updated_by  FOREIGN KEY (updated_by_user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. Sequence Registration
-- ============================================================
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('SPL', 'SPL-{date:yyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('PR', 'PR-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('PO', 'PO-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- ============================================================
-- 7. Permission Groups (Menu Entries)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('PUR-01', 'Daftar Harga Supplier', 'Supplier Price List',
 'Pengadaan > Daftar Harga Supplier', 'Procurement (Purchase) > Supplier Price List',
 '/purchasing/supplier-price-lists', 'ti-receipt',
 'Kelola daftar harga supplier', 'Manage supplier price lists',
 200, 1, NOW()),

('PUR-02', 'Permintaan Pembelian', 'Purchase Requisition',
 'Pengadaan > Permintaan Pembelian', 'Procurement (Purchase) > Purchase Requisition',
 '/purchasing/purchase-requisitions', 'ti-file-text',
 'Kelola permintaan pembelian (PR)', 'Manage purchase requisitions (PR)',
 201, 1, NOW()),

('PUR-03', 'Purchase Order', 'Purchase Order',
 'Pengadaan > Purchase Order', 'Procurement (Purchase) > Purchase Order',
 '/purchasing/purchase-orders', 'ti-shopping-cart',
 'Kelola purchase order (PO)', 'Manage purchase orders (PO)',
 202, 1, NOW());

-- ============================================================
-- 8. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
-- Supplier Price List permissions
('SPL_READ',   'Melihat daftar harga supplier',        1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),
('SPL_CREATE', 'Membuat daftar harga supplier baru',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),
('SPL_UPDATE', 'Mengubah daftar harga supplier',        1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),
('SPL_DELETE', 'Menghapus/nonaktifkan daftar harga supplier', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),
('LOOKUP_SUPPLIER-PRICE-LIST', 'Lookup daftar harga supplier untuk autocomplete', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-01')),

-- Purchase Requisition permissions
('PR_READ',   'Melihat daftar permintaan pembelian',   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('PR_CREATE', 'Membuat permintaan pembelian baru',      1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('PR_UPDATE', 'Mengubah permintaan pembelian',          1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('PR_DELETE', 'Menghapus permintaan pembelian',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('PR_SUBMIT', 'Mengajukan permintaan pembelian untuk persetujuan', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),
('LOOKUP_PR', 'Lookup permintaan pembelian untuk autocomplete', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-02')),

-- Purchase Order permissions
('PO_READ',    'Melihat daftar purchase order',                    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_CREATE',  'Membuat purchase order baru',                      1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_UPDATE',  'Mengubah purchase order',                          1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_DELETE',  'Menghapus purchase order',                         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_SUBMIT',  'Mengajukan purchase order untuk persetujuan',      1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_SEND',    'Mengirim purchase order ke supplier',              1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('LOOKUP_PO',  'Lookup purchase order untuk autocomplete',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03'));

-- ============================================================
-- 9. Grant all purchasing permissions to ROLE_ADMIN
-- ============================================================
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('SPL_READ', 'SPL_CREATE', 'SPL_UPDATE', 'SPL_DELETE', 'LOOKUP_SUPPLIER-PRICE-LIST',
                 'PR_READ', 'PR_CREATE', 'PR_UPDATE', 'PR_DELETE', 'PR_SUBMIT', 'LOOKUP_PR',
                 'PO_READ', 'PO_CREATE', 'PO_UPDATE', 'PO_DELETE', 'PO_SUBMIT', 'PO_SEND', 'LOOKUP_PO');
