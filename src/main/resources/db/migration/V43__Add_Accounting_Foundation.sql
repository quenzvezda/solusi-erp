-- V43: Accounting Foundation — COA, Schema, Fiscal Year, Periods, Permissions

-- ============================================================
-- 1. DDL: Chart of Accounts
-- ============================================================
CREATE TABLE acc_chart_of_accounts (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(20)  NOT NULL,
    name          VARCHAR(150) NOT NULL,
    account_type  VARCHAR(20)  NOT NULL COMMENT 'ASSET | LIABILITY | EQUITY | REVENUE | EXPENSE',
    normal_balance VARCHAR(10) NOT NULL COMMENT 'DEBIT | CREDIT',
    parent_id     BIGINT       NULL,
    level         INT          NOT NULL DEFAULT 1,
    is_header     BOOLEAN      NOT NULL DEFAULT FALSE,
    note          TEXT         NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    version       INT          NOT NULL DEFAULT 0,
    created_by_user_id BIGINT  NULL,
    created_date  DATETIME     NULL,
    updated_by_user_id BIGINT  NULL,
    updated_date  DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coa_code (code),
    CONSTRAINT fk_coa_parent FOREIGN KEY (parent_id) REFERENCES acc_chart_of_accounts(id),
    CONSTRAINT fk_coa_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_coa_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. DDL: Accounting Schema
-- ============================================================
CREATE TABLE acc_accounting_schemas (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    event_type        VARCHAR(50)  NOT NULL COMMENT 'e.g. GOODS_RECEIPT, VENDOR_BILL, etc.',
    description       VARCHAR(255) NULL,
    debit_account_id  BIGINT       NOT NULL,
    credit_account_id BIGINT       NOT NULL,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    version           INT          NOT NULL DEFAULT 0,
    created_by_user_id BIGINT     NULL,
    created_date      DATETIME     NULL,
    updated_by_user_id BIGINT     NULL,
    updated_date      DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_schema_event_active (event_type, is_active),
    CONSTRAINT fk_schema_debit  FOREIGN KEY (debit_account_id)  REFERENCES acc_chart_of_accounts(id),
    CONSTRAINT fk_schema_credit FOREIGN KEY (credit_account_id) REFERENCES acc_chart_of_accounts(id),
    CONSTRAINT fk_schema_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_schema_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. DDL: Fiscal Year
-- ============================================================
CREATE TABLE acc_fiscal_years (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(20)  NOT NULL,
    name          VARCHAR(100) NOT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    version       INT          NOT NULL DEFAULT 0,
    created_by_user_id BIGINT  NULL,
    created_date  DATETIME     NULL,
    updated_by_user_id BIGINT  NULL,
    updated_date  DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fy_code (code),
    CONSTRAINT fk_fy_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_fy_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. DDL: Accounting Period
-- ============================================================
CREATE TABLE acc_accounting_periods (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    code            VARCHAR(20)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    fiscal_year_id  BIGINT       NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    status          VARCHAR(10)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN | CLOSED',
    version         INT          NOT NULL DEFAULT 0,
    created_by_user_id BIGINT   NULL,
    created_date    DATETIME     NULL,
    updated_by_user_id BIGINT   NULL,
    updated_date    DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_period_code (code),
    CONSTRAINT fk_period_fy FOREIGN KEY (fiscal_year_id) REFERENCES acc_fiscal_years(id),
    CONSTRAINT fk_period_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_period_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. Sequence Generator Entry for Fiscal Year
-- ============================================================
INSERT INTO system_sequences (entity_name, format_pattern, pad_length, last_value, reset_cycle, created_by_user_id, created_date)
VALUES ('FISCAL_YEAR', 'FY-{seq}', 4, 0, 'NEVER', 1, NOW());

-- ============================================================
-- 6. Permission Groups (Menu Entries)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('GL-01', 'Bagan Akun', 'Chart of Accounts',
 'Keuangan & Akuntansi > Buku Besar > Bagan Akun', 'Finance & Accounting > General Ledger > Chart of Accounts',
 '/accounting/coa', 'ti-list-tree',
 'Kelola master bagan akun (COA)', 'Manage chart of accounts (COA) master data',
 100, 1, NOW()),

('GL-02', 'Skema Akuntansi', 'Accounting Schema',
 'Keuangan & Akuntansi > Buku Besar > Skema Akuntansi', 'Finance & Accounting > General Ledger > Accounting Schema',
 '/accounting/schemas', 'ti-route',
 'Konfigurasi mapping event ke akun debit/kredit', 'Configure event-to-account debit/credit mapping',
 101, 1, NOW()),

('GL-03', 'Periode Akuntansi', 'Accounting Period',
 'Keuangan & Akuntansi > Buku Besar > Periode Akuntansi', 'Finance & Accounting > General Ledger > Accounting Period',
 '/accounting/periods', 'ti-calendar',
 'Kelola tahun fiskal dan periode buku', 'Manage fiscal years and accounting periods',
 102, 1, NOW());

-- ============================================================
-- 7. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
-- COA permissions
('ACCOUNTING-COA_READ',   'Melihat daftar bagan akun',            1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),
('ACCOUNTING-COA_CREATE', 'Membuat akun baru pada bagan akun',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),
('ACCOUNTING-COA_UPDATE', 'Mengubah data akun pada bagan akun',   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),
('ACCOUNTING-COA_DELETE', 'Menghapus akun dari bagan akun',       1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),
('LOOKUP_COA',            'Lookup akun COA untuk autocomplete',   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-01')),

-- Schema permissions
('ACCOUNTING-SCHEMA_READ',   'Melihat daftar skema akuntansi',           1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-02')),
('ACCOUNTING-SCHEMA_CREATE', 'Membuat mapping skema akuntansi baru',     1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-02')),
('ACCOUNTING-SCHEMA_UPDATE', 'Mengubah mapping skema akuntansi',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-02')),
('ACCOUNTING-SCHEMA_DELETE', 'Menghapus mapping skema akuntansi',        1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-02')),

-- Period permissions
('ACCOUNTING-PERIOD_READ',   'Melihat daftar periode akuntansi',         1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-03')),
('ACCOUNTING-PERIOD_CREATE', 'Membuat tahun fiskal dan periode baru',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-03')),
('ACCOUNTING-PERIOD_UPDATE', 'Mengubah tahun fiskal dan periode',        1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-03')),
('ACCOUNTING-PERIOD_DELETE', 'Menghapus tahun fiskal dan periode',       1, NOW(), (SELECT id FROM permission_groups WHERE code = 'GL-03'));

-- ============================================================
-- 8. Grant all accounting permissions to ROLE_ADMIN
-- ============================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name LIKE 'ACCOUNTING-\_%' ESCAPE '\\';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name = 'LOOKUP_COA';
