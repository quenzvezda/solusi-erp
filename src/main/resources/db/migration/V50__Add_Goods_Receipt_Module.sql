-- V50: Goods Receipt Module (Sprint 4 Task 5)

-- ============================================================
-- 1. DDL: Goods Receipt (Header)
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_goods_receipts (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    code               VARCHAR(30)   NOT NULL,
    receipt_date       DATE          NOT NULL,
    po_id              BIGINT        NOT NULL,
    supplier_id        BIGINT        NOT NULL,
    facility_id        BIGINT        NULL,
    currency_id        BIGINT        NOT NULL,
    exchange_rate      DECIMAL(19,6) NOT NULL,
    status             VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    note               TEXT          NULL,
    version            INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT        NULL,
    created_date       DATETIME      NULL,
    updated_by_user_id BIGINT        NULL,
    updated_date       DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_gr_code (code),
    CONSTRAINT fk_gr_po            FOREIGN KEY (po_id)               REFERENCES pur_purchase_orders(id),
    CONSTRAINT fk_gr_supplier      FOREIGN KEY (supplier_id)         REFERENCES parties(id),
    CONSTRAINT fk_gr_facility      FOREIGN KEY (facility_id)         REFERENCES inv_facilities(id),
    CONSTRAINT fk_gr_currency      FOREIGN KEY (currency_id)         REFERENCES master_currencies(id),
    CONSTRAINT fk_gr_created_by    FOREIGN KEY (created_by_user_id)  REFERENCES users(id),
    CONSTRAINT fk_gr_updated_by    FOREIGN KEY (updated_by_user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. DDL: Goods Receipt Lines
-- ============================================================
CREATE TABLE IF NOT EXISTS pur_goods_receipt_lines (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    header_id          BIGINT        NOT NULL,
    po_line_id         BIGINT        NOT NULL,
    product_id         BIGINT        NOT NULL,
    quantity_received  DECIMAL(19,4) NOT NULL,
    uom_id             BIGINT        NOT NULL,
    container_id       BIGINT        NULL,
    base_quantity      DECIMAL(19,4) NOT NULL,
    inventory_amount   DECIMAL(19,4) NOT NULL,
    tax_base_amount    DECIMAL(19,4) NOT NULL,
    tax_amount         DECIMAL(19,4) NOT NULL,
    gr_ir_amount       DECIMAL(19,4) NOT NULL,
    serial_number      VARCHAR(100)  NULL,
    version            INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT        NULL,
    created_date       DATETIME      NULL,
    updated_by_user_id BIGINT        NULL,
    updated_date       DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_grl_header       FOREIGN KEY (header_id)           REFERENCES pur_goods_receipts(id) ON DELETE CASCADE,
    CONSTRAINT fk_grl_po_line      FOREIGN KEY (po_line_id)          REFERENCES pur_purchase_order_lines(id),
    CONSTRAINT fk_grl_product      FOREIGN KEY (product_id)          REFERENCES products(id),
    CONSTRAINT fk_grl_uom          FOREIGN KEY (uom_id)              REFERENCES unit_of_measures(id),
    CONSTRAINT fk_grl_container    FOREIGN KEY (container_id)        REFERENCES inv_containers(id),
    CONSTRAINT fk_grl_created_by   FOREIGN KEY (created_by_user_id)  REFERENCES users(id),
    CONSTRAINT fk_grl_updated_by   FOREIGN KEY (updated_by_user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. Sequence Registration
-- ============================================================
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('GOODS_RECEIPT', 'GR-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- ============================================================
-- 4. Permission Group (Menu Entry)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('INV-11', 'Penerimaan Barang', 'Goods Receipt',
 'Inventory > Penerimaan Barang', 'Inventory > Goods Receipt',
 '/inventory/goods-receipts', 'ti-package-import',
 'Kelola penerimaan barang dari PO', 'Manage goods receipt from PO',
 220, 1, NOW());

-- ============================================================
-- 5. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('GOODS-RECEIPT_READ',   'Melihat daftar penerimaan barang',           1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('GOODS-RECEIPT_CREATE', 'Membuat penerimaan barang baru',             1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('GOODS-RECEIPT_UPDATE', 'Mengubah penerimaan barang',                 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('GOODS-RECEIPT_DELETE', 'Menghapus penerimaan barang',                1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('GOODS-RECEIPT_COMPLETE', 'Menyelesaikan (complete) penerimaan barang', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11'));

-- ============================================================
-- 6. Grant all goods receipt permissions to ROLE_ADMIN
-- ============================================================
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('GOODS-RECEIPT_READ', 'GOODS-RECEIPT_CREATE', 'GOODS-RECEIPT_UPDATE', 'GOODS-RECEIPT_DELETE', 'GOODS-RECEIPT_COMPLETE');
