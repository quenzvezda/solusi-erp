-- V74: Debit Memo Allocation
-- Applies confirmed Debit Memo value to one or more confirmed Vendor Bills.

-- 1. DDL: Debit Memo Allocation Header
CREATE TABLE IF NOT EXISTS ap_debit_memo_allocations (
    id                           BIGINT        NOT NULL AUTO_INCREMENT,
    code                         VARCHAR(50)   NOT NULL,
    debit_memo_id                BIGINT        NOT NULL,
    debit_memo_code              VARCHAR(50)   NOT NULL,
    allocation_date              DATE          NOT NULL,
    status                       VARCHAR(30)   NOT NULL,
    total_applied_gross_original DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_dpp_original           DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_tax_original           DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_grir_reversal_base     DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_tax_reversal_base      DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_ap_reduction_base      DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_fx_loss_base           DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_fx_gain_base           DECIMAL(19,4) NOT NULL DEFAULT 0,
    apply_journal_entry_id       BIGINT        NULL,
    reversal_journal_entry_id    BIGINT        NULL,
    reversal_date                DATE          NULL,
    reversal_reason              VARCHAR(500)  NULL,
    notes                        VARCHAR(500)  NULL,
    created_by_user_id           BIGINT        NULL,
    created_date                 DATETIME      NOT NULL,
    updated_by_user_id           BIGINT        NULL,
    updated_date                 DATETIME      NOT NULL,
    version                      BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_ap_dma_code UNIQUE (code),
    KEY idx_ap_dma_debit_memo_status (debit_memo_id, status),
    KEY idx_ap_dma_status_date (status, allocation_date),
    KEY idx_ap_dma_keyword (code, debit_memo_code),
    KEY idx_ap_dma_apply_journal (apply_journal_entry_id),
    KEY idx_ap_dma_reversal_journal (reversal_journal_entry_id),
    CONSTRAINT fk_ap_dma_debit_memo       FOREIGN KEY (debit_memo_id)             REFERENCES ap_debit_memos(id),
    CONSTRAINT fk_ap_dma_apply_journal    FOREIGN KEY (apply_journal_entry_id)    REFERENCES acc_journal_entries(id),
    CONSTRAINT fk_ap_dma_reversal_journal FOREIGN KEY (reversal_journal_entry_id) REFERENCES acc_journal_entries(id),
    CONSTRAINT fk_ap_dma_created_by       FOREIGN KEY (created_by_user_id)        REFERENCES users(id),
    CONSTRAINT fk_ap_dma_updated_by       FOREIGN KEY (updated_by_user_id)        REFERENCES users(id)
);

-- 2. DDL: Debit Memo Allocation Lines
CREATE TABLE IF NOT EXISTS ap_debit_memo_allocation_lines (
    id                               BIGINT        NOT NULL AUTO_INCREMENT,
    debit_memo_allocation_id         BIGINT        NOT NULL,
    vendor_bill_id                   BIGINT        NOT NULL,
    vendor_bill_code                 VARCHAR(50)   NOT NULL,
    debit_memo_remaining_at_draft    DECIMAL(19,4) NOT NULL DEFAULT 0,
    vendor_bill_outstanding_at_draft DECIMAL(19,4) NOT NULL DEFAULT 0,
    applied_gross_original           DECIMAL(19,4) NOT NULL DEFAULT 0,
    applied_dpp_original             DECIMAL(19,4) NOT NULL DEFAULT 0,
    applied_tax_original             DECIMAL(19,4) NOT NULL DEFAULT 0,
    grir_reversal_base               DECIMAL(19,4) NOT NULL DEFAULT 0,
    tax_reversal_base                DECIMAL(19,4) NOT NULL DEFAULT 0,
    vendor_bill_exchange_rate        DECIMAL(19,6) NOT NULL DEFAULT 1.000000,
    ap_reduction_base                DECIMAL(19,4) NOT NULL DEFAULT 0,
    fx_loss_base                     DECIMAL(19,4) NOT NULL DEFAULT 0,
    fx_gain_base                     DECIMAL(19,4) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_ap_dma_lines_allocation_bill UNIQUE (debit_memo_allocation_id, vendor_bill_id),
    KEY idx_ap_dma_lines_allocation (debit_memo_allocation_id),
    KEY idx_ap_dma_lines_vendor_bill (vendor_bill_id),
    CONSTRAINT fk_ap_dma_line_allocation FOREIGN KEY (debit_memo_allocation_id) REFERENCES ap_debit_memo_allocations(id) ON DELETE CASCADE,
    CONSTRAINT fk_ap_dma_line_vendor_bill FOREIGN KEY (vendor_bill_id)           REFERENCES ap_vendor_bills(id)
);

-- 3. Sequence Registration
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('DEBIT_MEMO_ALLOCATION', 'DMA-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- 4. Permission Group (Menu Entry)
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('AP-04', 'Alokasi Debit Memo', 'Debit Memo Allocation',
 'Keuangan & Akuntansi > Hutang Usaha > Alokasi Debit Memo', 'Finance & Accounting > Account Payable > Debit Memo Allocation',
 '/accounts-payable/debit-memo-allocations', 'ti-file-check',
 'Kelola alokasi debit memo vendor ke tagihan vendor', 'Manage vendor debit memo allocations to vendor bills',
 330, 1, NOW());

-- 5. Permissions
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('DEBIT-MEMO-ALLOCATION_READ',    'Melihat daftar dan detail alokasi debit memo', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-04')),
('DEBIT-MEMO-ALLOCATION_CREATE',  'Membuat alokasi debit memo baru',             1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-04')),
('DEBIT-MEMO-ALLOCATION_UPDATE',  'Mengubah alokasi debit memo draft',           1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-04')),
('DEBIT-MEMO-ALLOCATION_CONFIRM', 'Mengonfirmasi alokasi debit memo',            1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-04')),
('DEBIT-MEMO-ALLOCATION_CANCEL',  'Membatalkan alokasi debit memo draft',        1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-04')),
('DEBIT-MEMO-ALLOCATION_REVERSE', 'Membalik alokasi debit memo terkonfirmasi',   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'AP-04'));

-- 6. Grant all debit memo allocation permissions to ROLE_ADMIN
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('DEBIT-MEMO-ALLOCATION_READ', 'DEBIT-MEMO-ALLOCATION_CREATE', 'DEBIT-MEMO-ALLOCATION_UPDATE',
                 'DEBIT-MEMO-ALLOCATION_CONFIRM', 'DEBIT-MEMO-ALLOCATION_CANCEL', 'DEBIT-MEMO-ALLOCATION_REVERSE');

-- 7. Accounting schema for Debit Memo Application
SET @schema_id = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'DEBIT_MEMO_APPLICATION' AND is_active = TRUE LIMIT 1);

INSERT INTO acc_accounting_schemas (event_type, description, is_active, version, created_by_user_id, created_date)
SELECT 'DEBIT_MEMO_APPLICATION', 'Apply vendor debit memo against vendor bill liability.', TRUE, 1, 1, NOW()
WHERE @schema_id IS NULL;

SET @schema_id = COALESCE(@schema_id, LAST_INSERT_ID());

DELETE FROM acc_schema_lines WHERE schema_id = @schema_id;

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'DMA_AP_AMT', id, 'DEBIT'
FROM acc_chart_of_accounts WHERE code = '2110';

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'DMA_GRIR_CLEARING_AMT', id, 'CREDIT'
FROM acc_chart_of_accounts WHERE code = '2120';

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'DMA_TAX_AMT', id, 'CREDIT'
FROM acc_chart_of_accounts WHERE code = '1230';

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'DMA_FX_LOSS_AMT', id, 'DEBIT'
FROM acc_chart_of_accounts WHERE code = '5140';

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'DMA_FX_GAIN_AMT', id, 'CREDIT'
FROM acc_chart_of_accounts WHERE code = '4240';
