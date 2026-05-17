-- V58: Vendor Bill Module (Accounts Payable)

-- Must exist:
-- ap_vendor_bills
-- ap_vendor_bill_gr_refs
-- ap_vendor_bill_lines
-- UNIQUE (vendor_id, vendor_invoice_number)
-- Permission group AP-01 + VENDOR-BILL_* permissions

-- ============================================================
-- 1. DDL: Vendor Bill (Header)
-- ============================================================
CREATE TABLE IF NOT EXISTS ap_vendor_bills (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    code                  VARCHAR(50)   NOT NULL,
    vendor_id             BIGINT        NOT NULL,
    vendor_invoice_number VARCHAR(100)  NOT NULL,
    bill_date             DATE          NOT NULL,
    due_date              DATE          NOT NULL,
    currency_id           BIGINT        NOT NULL,
    status                VARCHAR(30)   NOT NULL,
    subtotal              DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount            DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount          DECIMAL(19,4) NOT NULL DEFAULT 0,
    notes                 VARCHAR(500)  NULL,
    created_by_user_id    BIGINT        NULL,
    created_date          DATETIME      NOT NULL,
    updated_by_user_id    BIGINT        NULL,
    updated_date          DATETIME      NOT NULL,
    version               BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ap_vendor_bills_code (code),
    UNIQUE KEY uk_ap_vendor_bills_vendor_invoice (vendor_id, vendor_invoice_number),
    CONSTRAINT fk_ap_vb_vendor     FOREIGN KEY (vendor_id)            REFERENCES parties(id),
    CONSTRAINT fk_ap_vb_currency   FOREIGN KEY (currency_id)          REFERENCES master_currencies(id),
    CONSTRAINT fk_ap_vb_created_by FOREIGN KEY (created_by_user_id)   REFERENCES users(id),
    CONSTRAINT fk_ap_vb_updated_by FOREIGN KEY (updated_by_user_id)   REFERENCES users(id)
);

-- ============================================================
-- 2. DDL: Vendor Bill Goods Receipt References
-- ============================================================
CREATE TABLE IF NOT EXISTS ap_vendor_bill_gr_refs (
    bill_id BIGINT NOT NULL,
    gr_id   BIGINT NOT NULL,
    PRIMARY KEY (bill_id, gr_id),
    CONSTRAINT fk_ap_vb_ref_bill FOREIGN KEY (bill_id) REFERENCES ap_vendor_bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_ap_vb_ref_gr   FOREIGN KEY (gr_id)   REFERENCES pur_goods_receipts(id)
);

-- ============================================================
-- 3. DDL: Vendor Bill Lines
-- ============================================================
CREATE TABLE IF NOT EXISTS ap_vendor_bill_lines (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    bill_id          BIGINT        NOT NULL,
    gr_line_id       BIGINT        NOT NULL,
    product_id       BIGINT        NOT NULL,
    product_name     VARCHAR(255)  NOT NULL,
    description      VARCHAR(500)  NULL,
    qty_billed       DECIMAL(19,4) NOT NULL,
    uom_id           BIGINT        NOT NULL,
    uom_name         VARCHAR(100)  NOT NULL,
    unit_price       DECIMAL(19,4) NOT NULL,
    inventory_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount       DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_total       DECIMAL(19,4) NOT NULL DEFAULT 0,
    created_by_user_id BIGINT     NULL,
    created_date     DATETIME     NOT NULL,
    updated_by_user_id BIGINT     NULL,
    updated_date     DATETIME     NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    KEY idx_ap_vb_lines_bill (bill_id),
    KEY idx_ap_vb_lines_gr_line (gr_line_id),
    CONSTRAINT fk_ap_vb_line_bill       FOREIGN KEY (bill_id)            REFERENCES ap_vendor_bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_ap_vb_line_gr_line    FOREIGN KEY (gr_line_id)         REFERENCES pur_goods_receipt_lines(id),
    CONSTRAINT fk_ap_vb_line_product    FOREIGN KEY (product_id)         REFERENCES products(id),
    CONSTRAINT fk_ap_vb_line_uom        FOREIGN KEY (uom_id)             REFERENCES unit_of_measures(id),
    CONSTRAINT fk_ap_vb_line_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_ap_vb_line_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
);

-- ============================================================
-- 4. Sequence Registration
-- ============================================================
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('VENDOR-BILL', 'VB-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- ============================================================
-- 5. Permission Group (Menu Entry)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('AP-01', 'Tagihan Vendor', 'Vendor Bill',
 'Hutang Usaha > Tagihan Vendor', 'Accounts Payable > Vendor Bill',
 '/accounts-payable/vendor-bills', 'ti-file-invoice',
 'Kelola tagihan vendor dari penerimaan barang', 'Manage vendor bills from goods receipts',
 300, 1, NOW());

-- ============================================================
-- 6. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('VENDOR-BILL_READ',    'Melihat daftar tagihan vendor',              1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-01')),
('VENDOR-BILL_CREATE',  'Membuat tagihan vendor baru',                1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-01')),
('VENDOR-BILL_UPDATE',  'Mengubah tagihan vendor',                    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-01')),
('VENDOR-BILL_DELETE',  'Menghapus tagihan vendor',                   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-01')),
('VENDOR-BILL_CONFIRM', 'Mengonfirmasi tagihan vendor',               1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-01')),
('VENDOR-BILL_CANCEL',  'Membatalkan tagihan vendor',                 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-01'));

-- ============================================================
-- 7. Grant all vendor bill permissions to ROLE_ADMIN
-- ============================================================
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('VENDOR-BILL_READ', 'VENDOR-BILL_CREATE', 'VENDOR-BILL_UPDATE',
                 'VENDOR-BILL_DELETE', 'VENDOR-BILL_CONFIRM', 'VENDOR-BILL_CANCEL');
