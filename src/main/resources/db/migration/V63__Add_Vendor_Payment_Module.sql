-- V63__Add_Vendor_Payment_Module.sql
-- Vendor Payment module: tables, permissions, menu, role grants

-- 1. DDL: Vendor Payment (Header)
CREATE TABLE IF NOT EXISTS ap_vendor_payments (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    code                  VARCHAR(50)   NOT NULL,
    vendor_id             BIGINT        NOT NULL,
    currency_id           BIGINT        NOT NULL,
    bank_account_id       BIGINT        NOT NULL,
    payment_date          DATE          NOT NULL,
    exchange_rate         DECIMAL(19,6) NOT NULL DEFAULT 1.000000,
    payment_amount        DECIMAL(19,4) NOT NULL DEFAULT 0,
    status                VARCHAR(30)   NOT NULL,
    reference             VARCHAR(255)  NULL,
    notes                 VARCHAR(500)  NULL,
    created_by_user_id    BIGINT        NULL,
    created_date          DATETIME      NOT NULL,
    updated_by_user_id    BIGINT        NULL,
    updated_date          DATETIME      NOT NULL,
    version               BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ap_vendor_payments_code (code),
    CONSTRAINT fk_ap_vp_vendor       FOREIGN KEY (vendor_id)          REFERENCES parties(id),
    CONSTRAINT fk_ap_vp_currency     FOREIGN KEY (currency_id)        REFERENCES master_currencies(id),
    CONSTRAINT fk_ap_vp_bank_account FOREIGN KEY (bank_account_id)    REFERENCES bank_accounts(id),
    CONSTRAINT fk_ap_vp_created_by   FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_ap_vp_updated_by   FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. DDL: Vendor Payment Lines (allocation to vendor bills)
CREATE TABLE IF NOT EXISTS ap_vendor_payment_lines (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    vendor_payment_id     BIGINT        NOT NULL,
    vendor_bill_id        BIGINT        NOT NULL,
    bill_code             VARCHAR(50)   NOT NULL,
    outstanding_amount    DECIMAL(19,4) NOT NULL DEFAULT 0,
    paid_amount           DECIMAL(19,4) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ap_vp_lines_payment (vendor_payment_id),
    KEY idx_ap_vp_lines_bill (vendor_bill_id),
    CONSTRAINT fk_ap_vp_line_payment FOREIGN KEY (vendor_payment_id) REFERENCES ap_vendor_payments(id) ON DELETE CASCADE,
    CONSTRAINT fk_ap_vp_line_bill    FOREIGN KEY (vendor_bill_id)    REFERENCES ap_vendor_bills(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Sequence Registration
INSERT INTO system_sequences (mod_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('VENDOR-PAYMENT', 'VP-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE mod_code = mod_code;

-- 4. Permission Group (Menu Entry)
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, desc_id, desc_en, sort_order, created_by_user_id, created_date)
VALUES
('AP-02', 'Pembayaran Vendor', 'Vendor Payment',
 'Hutang Usaha > Pembayaran Vendor', 'Accounts Payable > Vendor Payment',
 '/accounts-payable/vendor-payments', 'ti-cash',
 'Kelola pembayaran ke vendor', 'Manage payments to vendors',
 310, 1, NOW());

-- 5. Permissions
INSERT INTO permissions (name, `desc`, created_by_user_id, created_date, permission_group_id) VALUES
('VENDOR-PAYMENT_READ',    'Melihat daftar pembayaran vendor',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-02')),
('VENDOR-PAYMENT_CREATE',  'Membuat pembayaran vendor baru',      1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-02')),
('VENDOR-PAYMENT_UPDATE',  'Mengubah pembayaran vendor',          1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-02')),
('VENDOR-PAYMENT_DELETE',  'Menghapus pembayaran vendor',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-02')),
('VENDOR-PAYMENT_CONFIRM', 'Mengonfirmasi pembayaran vendor',     1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-02')),
('VENDOR-PAYMENT_CANCEL',  'Membatalkan pembayaran vendor',       1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-02'));

-- 6. Grant all vendor payment permissions to ROLE_ADMIN
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('VENDOR-PAYMENT_READ', 'VENDOR-PAYMENT_CREATE', 'VENDOR-PAYMENT_UPDATE',
                 'VENDOR-PAYMENT_DELETE', 'VENDOR-PAYMENT_CONFIRM', 'VENDOR-PAYMENT_CANCEL');
