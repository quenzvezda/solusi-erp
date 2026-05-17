ALTER TABLE acc_journal_lines ADD COLUMN original_currency_id BIGINT NULL;
ALTER TABLE acc_journal_lines ADD COLUMN exchange_rate DECIMAL(19,6) NULL;
ALTER TABLE acc_journal_lines ADD COLUMN original_debit_amount DECIMAL(19,4) NULL DEFAULT NULL;
ALTER TABLE acc_journal_lines ADD COLUMN original_credit_amount DECIMAL(19,4) NULL DEFAULT NULL;
ALTER TABLE acc_journal_lines ADD CONSTRAINT fk_acc_journal_lines_original_currency
        FOREIGN KEY (original_currency_id) REFERENCES master_currencies(id);

ALTER TABLE ap_vendor_bills
    ADD COLUMN exchange_rate DECIMAL(19,6) NOT NULL DEFAULT 1.000000;
