ALTER TABLE acc_journal_entries
    ADD COLUMN currency_id BIGINT NULL AFTER source_code,
    ADD COLUMN exchange_rate DECIMAL(19,6) NULL AFTER currency_id,
    ADD COLUMN reference_no VARCHAR(100) NULL AFTER exchange_rate,
    ADD COLUMN reversal_of_id BIGINT NULL AFTER reference_no,
    MODIFY COLUMN source_id BIGINT NULL,
    ADD CONSTRAINT fk_acc_journal_entries_currency
        FOREIGN KEY (currency_id) REFERENCES master_currencies(id),
    ADD CONSTRAINT fk_acc_journal_entries_reversal_of
        FOREIGN KEY (reversal_of_id) REFERENCES acc_journal_entries(id),
    ADD CONSTRAINT uk_acc_journal_entries_reversal_of UNIQUE (reversal_of_id);

ALTER TABLE acc_journal_lines
    ADD COLUMN description VARCHAR(255) NULL AFTER original_credit_amount;

INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('JOURNAL-ENTRY_CREATE',  'Membuat entri jurnal manual',             1, NOW(), (SELECT id FROM permission_groups WHERE code = 'ACC-05')),
('JOURNAL-ENTRY_UPDATE',  'Mengubah draft entri jurnal manual',      1, NOW(), (SELECT id FROM permission_groups WHERE code = 'ACC-05')),
('JOURNAL-ENTRY_DELETE',  'Menghapus draft entri jurnal manual',     1, NOW(), (SELECT id FROM permission_groups WHERE code = 'ACC-05')),
('JOURNAL-ENTRY_POST',    'Memposting entri jurnal manual',          1, NOW(), (SELECT id FROM permission_groups WHERE code = 'ACC-05')),
('JOURNAL-ENTRY_REVERSE', 'Membuat reversal entri jurnal manual',    1, NOW(), (SELECT id FROM permission_groups WHERE code = 'ACC-05'));

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('JOURNAL-ENTRY_CREATE', 'JOURNAL-ENTRY_UPDATE', 'JOURNAL-ENTRY_DELETE',
                 'JOURNAL-ENTRY_POST', 'JOURNAL-ENTRY_REVERSE');
