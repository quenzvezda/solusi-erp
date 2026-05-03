CREATE TABLE acc_journal_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(50) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id BIGINT NOT NULL,
    source_code VARCHAR(60),
    posting_date DATE NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_by_user_id BIGINT,
    created_date DATETIME,
    updated_by_user_id BIGINT,
    updated_date DATETIME,
    version INT DEFAULT 1,
    CONSTRAINT uk_acc_journal_source UNIQUE (source_type, source_id)
);

CREATE TABLE acc_journal_lines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    journal_entry_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    account_id BIGINT NOT NULL,
    debit_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    credit_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    created_by_user_id BIGINT,
    created_date DATETIME,
    updated_by_user_id BIGINT,
    updated_date DATETIME,
    version INT DEFAULT 1,
    CONSTRAINT fk_acc_journal_lines_entry FOREIGN KEY (journal_entry_id)
        REFERENCES acc_journal_entries(id) ON DELETE CASCADE
);

-- ============================================================
-- 3. Permission Group (Menu Entry)
-- ============================================================
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('ACC-05', 'Entri Jurnal', 'Journal Entry',
 'Keuangan & Akuntansi > Buku Besar > Entri Jurnal', 'Finance & Accounting > General Ledger > Journal Entry',
 '/accounting/journal-entries', 'ti-receipt-2',
 'Kelola entri jurnal', 'Manage journal entries',
 350, 1, NOW());

-- ============================================================
-- 4. Permissions
-- ============================================================
INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('JOURNAL-ENTRY_READ',   'Melihat daftar dan detail entri jurnal', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'ACC-05'));

-- ============================================================
-- 5. Grant all journal entry permissions to ROLE_ADMIN
-- ============================================================
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('JOURNAL-ENTRY_READ');
