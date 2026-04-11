-- V47: Purchase Order Module — PO Header, PO Lines, Permissions

-- ============================================================
-- 1. DDL: Purchase Order (Header)
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_orders (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    code             VARCHAR(30)   NOT NULL,
    order_date       DATE          NOT NULL,
    expected_date    DATE          NULL,
    supplier_id      BIGINT        NOT NULL,
    facility_id      BIGINT        NULL,
    currency_id      BIGINT        NOT NULL,
    exchange_rate    DECIMAL(19,6) NOT NULL,
    subtotal         DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount       DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount     DECIMAL(19,4) NOT NULL DEFAULT 0,
    status           VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    payment_term_days INT          NOT NULL DEFAULT 30,
    pr_id            BIGINT        NULL,
    note             TEXT          NULL,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    version          INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT     NULL,
    created_date     DATETIME      NULL,
    updated_by_user_id BIGINT     NULL,
    updated_date     DATETIME      NULL,
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
-- 2. DDL: Purchase Order Lines
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_purchase_order_lines (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    header_id         BIGINT        NOT NULL,
    product_id        BIGINT        NOT NULL,
    quantity          DECIMAL(19,4) NOT NULL,
    received_quantity DECIMAL(19,4) NOT NULL DEFAULT 0,
    uom_id            BIGINT        NOT NULL,
    unit_price        DECIMAL(19,4) NOT NULL,
    tax_rate          DECIMAL(5,4)  NOT NULL DEFAULT 0,
    line_subtotal     DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_tax          DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_total        DECIMAL(19,4) NOT NULL DEFAULT 0,
    pr_line_id        BIGINT        NULL,
    note              TEXT          NULL,
    version           INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT      NULL,
    created_date      DATETIME      NULL,
    updated_by_user_id BIGINT      NULL,
    updated_date      DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pol_header     FOREIGN KEY (header_id)           REFERENCES pur_purchase_orders(id),
    CONSTRAINT fk_pol_product    FOREIGN KEY (product_id)          REFERENCES products(id),
    CONSTRAINT fk_pol_uom        FOREIGN KEY (uom_id)              REFERENCES unit_of_measures(id),
    CONSTRAINT fk_pol_pr_line    FOREIGN KEY (pr_line_id)          REFERENCES pur_purchase_requisition_lines(id),
    CONSTRAINT fk_pol_created_by FOREIGN KEY (created_by_user_id)  REFERENCES users(id),
    CONSTRAINT fk_pol_updated_by FOREIGN KEY (updated_by_user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. Sequence Registration
-- ============================================================
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('PO', 'PO-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- ============================================================
-- 4. Permission Group (Menu Entry)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('PUR-03', 'Purchase Order', 'Purchase Order',
 'Pengadaan > Purchase Order', 'Procurement (Purchase) > Purchase Order',
 '/purchasing/purchase-orders', 'ti-shopping-cart',
 'Kelola purchase order (PO)', 'Manage purchase orders (PO)',
 202, 1, NOW());

-- ============================================================
-- 5. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('PO_READ',    'Melihat daftar purchase order',                    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_CREATE',  'Membuat purchase order baru',                      1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_UPDATE',  'Mengubah purchase order',                          1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_DELETE',  'Menghapus purchase order',                         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_SUBMIT',  'Mengajukan purchase order untuk persetujuan',      1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('PO_SEND',    'Mengirim purchase order ke supplier',              1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03')),
('LOOKUP_PO',  'Lookup purchase order untuk autocomplete',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-03'));

-- ============================================================
-- 6. Grant all PO permissions to ROLE_ADMIN
-- ============================================================
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('PO_READ', 'PO_CREATE', 'PO_UPDATE', 'PO_DELETE', 'PO_SUBMIT', 'PO_SEND', 'LOOKUP_PO');
