-- V73: Debit Memo Core
-- Vendor Debit Memo records supplier credit generated from confirmed Purchase Return.

-- ============================================================
-- 1. DDL: Debit Memo Header
-- ============================================================
CREATE TABLE IF NOT EXISTS ap_debit_memos (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    code                  VARCHAR(50)   NOT NULL,
    purchase_return_id    BIGINT        NOT NULL,
    purchase_return_code  VARCHAR(60)   NOT NULL,
    vendor_id             BIGINT        NOT NULL,
    currency_id           BIGINT        NOT NULL,
    memo_date             DATE          NOT NULL,
    gross_amount_original DECIMAL(19,4) NOT NULL DEFAULT 0,
    dpp_amount_original   DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount_original   DECIMAL(19,4) NOT NULL DEFAULT 0,
    gross_amount_base     DECIMAL(19,4) NOT NULL DEFAULT 0,
    dpp_amount_base       DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount_base       DECIMAL(19,4) NOT NULL DEFAULT 0,
    settlement_status     VARCHAR(30)   NOT NULL,
    supplier_memo_number  VARCHAR(100)  NULL,
    supplier_memo_date    DATE          NULL,
    tax_document_number   VARCHAR(100)  NULL,
    tax_document_date     DATE          NULL,
    notes                 VARCHAR(500)  NULL,
    created_by_user_id    BIGINT        NULL,
    created_date          DATETIME      NOT NULL,
    updated_by_user_id    BIGINT        NULL,
    updated_date          DATETIME      NOT NULL,
    version               BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_ap_debit_memos_code UNIQUE (code),
    CONSTRAINT uk_ap_debit_memos_purchase_return UNIQUE (purchase_return_id),
    CONSTRAINT uk_ap_debit_memos_vendor_supplier_memo UNIQUE (vendor_id, supplier_memo_number),
    CONSTRAINT uk_ap_debit_memos_tax_document UNIQUE (tax_document_number),
    KEY idx_ap_debit_memos_vendor_status_date (vendor_id, settlement_status, memo_date),
    KEY idx_ap_debit_memos_memo_date (memo_date),
    KEY idx_ap_debit_memos_keyword (code, purchase_return_code, supplier_memo_number),
    CONSTRAINT fk_ap_dm_purchase_return FOREIGN KEY (purchase_return_id)    REFERENCES pur_purchase_returns(id),
    CONSTRAINT fk_ap_dm_vendor          FOREIGN KEY (vendor_id)             REFERENCES parties(id),
    CONSTRAINT fk_ap_dm_currency        FOREIGN KEY (currency_id)           REFERENCES master_currencies(id),
    CONSTRAINT fk_ap_dm_created_by      FOREIGN KEY (created_by_user_id)    REFERENCES users(id),
    CONSTRAINT fk_ap_dm_updated_by      FOREIGN KEY (updated_by_user_id)    REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. DDL: Debit Memo Lines
-- ============================================================
CREATE TABLE IF NOT EXISTS ap_debit_memo_lines (
    id                      BIGINT        NOT NULL AUTO_INCREMENT,
    debit_memo_id           BIGINT        NOT NULL,
    purchase_return_line_id BIGINT        NOT NULL,
    product_id              BIGINT        NOT NULL,
    quantity                DECIMAL(19,4) NOT NULL,
    uom_id                  BIGINT        NOT NULL,
    dpp_amount_original     DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount_original     DECIMAL(19,4) NOT NULL DEFAULT 0,
    dpp_amount_base         DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_amount_base         DECIMAL(19,4) NOT NULL DEFAULT 0,
    created_by_user_id      BIGINT        NULL,
    created_date            DATETIME      NOT NULL,
    updated_by_user_id      BIGINT        NULL,
    updated_date            DATETIME      NOT NULL,
    version                 BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    KEY idx_ap_dm_lines_memo (debit_memo_id),
    KEY idx_ap_dm_lines_purchase_return_line (purchase_return_line_id),
    KEY idx_ap_dm_lines_product (product_id),
    CONSTRAINT fk_ap_dm_line_memo                 FOREIGN KEY (debit_memo_id)           REFERENCES ap_debit_memos(id) ON DELETE CASCADE,
    CONSTRAINT fk_ap_dm_line_purchase_return_line FOREIGN KEY (purchase_return_line_id) REFERENCES pur_purchase_return_lines(id),
    CONSTRAINT fk_ap_dm_line_product              FOREIGN KEY (product_id)              REFERENCES products(id),
    CONSTRAINT fk_ap_dm_line_uom                  FOREIGN KEY (uom_id)                  REFERENCES unit_of_measures(id),
    CONSTRAINT fk_ap_dm_line_created_by           FOREIGN KEY (created_by_user_id)      REFERENCES users(id),
    CONSTRAINT fk_ap_dm_line_updated_by           FOREIGN KEY (updated_by_user_id)      REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. Sequence Registration
-- ============================================================
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('DEBIT_MEMO', 'DM-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- ============================================================
-- 4. Permission Group (Menu Entry)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('AP-03', 'Debit Memo', 'Debit Memo',
 'Keuangan & Akuntansi > Hutang Usaha > Debit Memo', 'Finance & Accounting > Account Payable > Debit Memo',
 '/accounts-payable/debit-memos', 'ti-file-minus',
 'Kelola debit memo vendor dari retur pembelian', 'Manage vendor debit memos from purchase returns',
 320, 1, NOW());

-- ============================================================
-- 5. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('DEBIT-MEMO_READ',            'Melihat daftar debit memo vendor',           1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-03')),
('DEBIT-MEMO_UPDATE-METADATA', 'Mengubah metadata eksternal debit memo',     1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-03')),
('DEBIT-MEMO_CANCEL',          'Membatalkan debit memo vendor yang terbuka', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-03'));

-- ============================================================
-- 6. Grant debit memo permissions to ROLE_ADMIN
-- ============================================================
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('DEBIT-MEMO_READ', 'DEBIT-MEMO_UPDATE-METADATA', 'DEBIT-MEMO_CANCEL');
