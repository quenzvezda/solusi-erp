-- V67: Purchase Return Phase 1 and generic inventory reservation

-- 1. Generic inventory reservation ownership
CREATE TABLE IF NOT EXISTS inv_stock_reservations (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    owner_ref_type        VARCHAR(50)   NOT NULL,
    owner_ref_id          BIGINT        NOT NULL,
    owner_ref_code        VARCHAR(60)   NOT NULL,
    product_id            BIGINT        NOT NULL,
    facility_id           BIGINT        NOT NULL,
    grid_id               BIGINT        NOT NULL,
    container_id          BIGINT        NOT NULL,
    serial_number         VARCHAR(100)  NULL,
    valuation_ref_type    VARCHAR(50)   NOT NULL,
    valuation_ref_id      BIGINT        NOT NULL,
    valuation_ref_line_id BIGINT        NOT NULL,
    quantity              DECIMAL(19,4) NOT NULL,
    status                VARCHAR(30)   NOT NULL,
    version               INT           NOT NULL DEFAULT 0,
    created_by_user_id    BIGINT        NULL,
    created_date          DATETIME      NULL,
    updated_by_user_id    BIGINT        NULL,
    updated_date          DATETIME      NULL,
    PRIMARY KEY (id),
    KEY idx_stock_res_owner_status (owner_ref_type, owner_ref_id, status),
    KEY idx_stock_res_active_container (status, product_id, facility_id, grid_id, container_id, quantity),
    KEY idx_stock_res_valuation (status, valuation_ref_type, valuation_ref_id, valuation_ref_line_id),
    KEY idx_stock_res_serial (status, product_id, serial_number),
    CONSTRAINT fk_stock_res_product    FOREIGN KEY (product_id)           REFERENCES products(id),
    CONSTRAINT fk_stock_res_facility   FOREIGN KEY (facility_id)          REFERENCES inv_facilities(id),
    CONSTRAINT fk_stock_res_grid       FOREIGN KEY (grid_id)              REFERENCES inv_grids(id),
    CONSTRAINT fk_stock_res_container  FOREIGN KEY (container_id)         REFERENCES inv_containers(id),
    CONSTRAINT fk_stock_res_created_by FOREIGN KEY (created_by_user_id)   REFERENCES users(id),
    CONSTRAINT fk_stock_res_updated_by FOREIGN KEY (updated_by_user_id)   REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Purchase Return header
CREATE TABLE IF NOT EXISTS pur_purchase_returns (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    code                  VARCHAR(30)   NOT NULL,
    return_date           DATE          NOT NULL,
    reference_type        VARCHAR(40)   NOT NULL DEFAULT 'GOODS_RECEIPT',
    reference_id          BIGINT        NOT NULL,
    reference_code        VARCHAR(60)   NOT NULL,
    purchase_order_id     BIGINT        NULL,
    purchase_order_code   VARCHAR(60)   NULL,
    supplier_id           BIGINT        NOT NULL,
    facility_id           BIGINT        NOT NULL,
    currency_id           BIGINT        NOT NULL,
    exchange_rate         DECIMAL(19,6) NOT NULL,
    status                VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    reason_code           VARCHAR(40)   NOT NULL,
    note                  TEXT          NULL,
    submitted_by_user_id  BIGINT        NULL,
    generated_gi_id       BIGINT        NULL,
    version               INT           NOT NULL DEFAULT 0,
    created_by_user_id    BIGINT        NULL,
    created_date          DATETIME      NULL,
    updated_by_user_id    BIGINT        NULL,
    updated_date          DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_purchase_return_code (code),
    UNIQUE KEY uk_purchase_return_generated_gi (generated_gi_id),
    KEY idx_purchase_return_date (return_date),
    KEY idx_purchase_return_status (status),
    KEY idx_purchase_return_supplier (supplier_id),
    KEY idx_purchase_return_source_gr (reference_type, reference_id),
    KEY idx_purchase_return_source_po (purchase_order_id),
    CONSTRAINT fk_purchase_return_po           FOREIGN KEY (purchase_order_id)    REFERENCES pur_purchase_orders(id),
    CONSTRAINT fk_purchase_return_supplier     FOREIGN KEY (supplier_id)          REFERENCES parties(id),
    CONSTRAINT fk_purchase_return_facility     FOREIGN KEY (facility_id)          REFERENCES inv_facilities(id),
    CONSTRAINT fk_purchase_return_currency     FOREIGN KEY (currency_id)          REFERENCES master_currencies(id),
    CONSTRAINT fk_purchase_return_submitted_by FOREIGN KEY (submitted_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_purchase_return_generated_gi FOREIGN KEY (generated_gi_id)      REFERENCES inv_goods_issues(id),
    CONSTRAINT fk_purchase_return_created_by   FOREIGN KEY (created_by_user_id)   REFERENCES users(id),
    CONSTRAINT fk_purchase_return_updated_by   FOREIGN KEY (updated_by_user_id)   REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Purchase Return lines
CREATE TABLE IF NOT EXISTS pur_purchase_return_lines (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    header_id             BIGINT        NOT NULL,
    goods_receipt_line_id BIGINT        NOT NULL,
    product_id            BIGINT        NOT NULL,
    is_serialized         BOOLEAN       NOT NULL DEFAULT FALSE,
    quantity              DECIMAL(19,4) NOT NULL,
    uom_id                BIGINT        NOT NULL,
    base_quantity         DECIMAL(19,4) NOT NULL,
    facility_id           BIGINT        NOT NULL,
    grid_id               BIGINT        NOT NULL,
    container_id          BIGINT        NOT NULL,
    serial_numbers        VARCHAR(1000) NULL,
    reason_code           VARCHAR(40)   NOT NULL,
    note                  TEXT          NULL,
    valuation_ref_type    VARCHAR(50)   NOT NULL,
    valuation_ref_id      BIGINT        NOT NULL,
    valuation_ref_line_id BIGINT        NOT NULL,
    unit_cost             DECIMAL(19,6) NOT NULL,
    inventory_amount      DECIMAL(19,4) NOT NULL,
    tax_reversal_amount   DECIMAL(19,4) NOT NULL DEFAULT 0,
    clearing_amount       DECIMAL(19,4) NOT NULL DEFAULT 0,
    version               INT           NOT NULL DEFAULT 0,
    created_by_user_id    BIGINT        NULL,
    created_date          DATETIME      NULL,
    updated_by_user_id    BIGINT        NULL,
    updated_date          DATETIME      NULL,
    PRIMARY KEY (id),
    KEY idx_purchase_return_line_header (header_id),
    KEY idx_purchase_return_line_gr (goods_receipt_line_id),
    KEY idx_purchase_return_line_valuation (valuation_ref_type, valuation_ref_id, valuation_ref_line_id),
    CONSTRAINT fk_purchase_return_line_header     FOREIGN KEY (header_id)             REFERENCES pur_purchase_returns(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_return_line_gr         FOREIGN KEY (goods_receipt_line_id) REFERENCES pur_goods_receipt_lines(id),
    CONSTRAINT fk_purchase_return_line_product    FOREIGN KEY (product_id)            REFERENCES products(id),
    CONSTRAINT fk_purchase_return_line_uom        FOREIGN KEY (uom_id)                REFERENCES unit_of_measures(id),
    CONSTRAINT fk_purchase_return_line_facility   FOREIGN KEY (facility_id)           REFERENCES inv_facilities(id),
    CONSTRAINT fk_purchase_return_line_grid       FOREIGN KEY (grid_id)               REFERENCES inv_grids(id),
    CONSTRAINT fk_purchase_return_line_container  FOREIGN KEY (container_id)          REFERENCES inv_containers(id),
    CONSTRAINT fk_purchase_return_line_created_by FOREIGN KEY (created_by_user_id)    REFERENCES users(id),
    CONSTRAINT fk_purchase_return_line_updated_by FOREIGN KEY (updated_by_user_id)    REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Purchase Return sequence
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('PURCHASE_RETURN', 'PRT-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- 5. Permission Group (Menu Entry)
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('PUR-04', 'Retur Pembelian', 'Purchase Return',
 'Pengadaan > Retur Pembelian', 'Procurement (Purchase) > Purchase Return',
 '/purchasing/purchase-returns', 'ti-package-export',
 'Kelola retur pembelian ke supplier', 'Manage supplier purchase returns',
 203, 1, NOW());

-- 6. Permissions
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('PURCHASE-RETURN_READ',    'Melihat daftar retur pembelian',                  1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-04')),
('PURCHASE-RETURN_CREATE',  'Membuat retur pembelian baru',                    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-04')),
('PURCHASE-RETURN_UPDATE',  'Mengubah retur pembelian draft',                  1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-04')),
('PURCHASE-RETURN_SUBMIT',  'Mengajukan retur pembelian untuk persetujuan',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-04')),
('PURCHASE-RETURN_CONFIRM', 'Mengonfirmasi retur pembelian yang disetujui',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-04')),
('PURCHASE-RETURN_CANCEL',  'Membatalkan pengajuan atau retur pembelian',      1, NOW(), (SELECT id FROM permission_groups WHERE code = 'PUR-04'));

-- 7. Grant all Purchase Return permissions to ROLE_ADMIN
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('PURCHASE-RETURN_READ', 'PURCHASE-RETURN_CREATE', 'PURCHASE-RETURN_UPDATE',
                 'PURCHASE-RETURN_SUBMIT', 'PURCHASE-RETURN_CONFIRM', 'PURCHASE-RETURN_CANCEL');
