-- V61__Bank_Account_Refactor_For_Payment.sql
-- Add currency_id and coa_id to bank_accounts for Vendor Payment integration

ALTER TABLE bank_accounts
    ADD COLUMN currency_id BIGINT NULL AFTER is_active,
    ADD COLUMN coa_id BIGINT NULL AFTER currency_id,
    ADD CONSTRAINT fk_bank_account_currency FOREIGN KEY (currency_id) REFERENCES master_currencies(id),
    ADD CONSTRAINT fk_bank_account_coa FOREIGN KEY (coa_id) REFERENCES acc_chart_of_accounts(id);

-- Update seeder data with currency and COA references
-- BA-DEMO-01 (Bank BCA) → IDR, COA 1120 (Bank)
UPDATE bank_accounts SET currency_id = (SELECT id FROM master_currencies WHERE code = 'IDR'),
    coa_id = (SELECT id FROM acc_chart_of_accounts WHERE code = '1120')
WHERE code = 'BA-0001';

-- BA-DEMO-02 (Bank Mandiri) → IDR, COA 1130 (Bank)
UPDATE bank_accounts SET currency_id = (SELECT id FROM master_currencies WHERE code = 'IDR'),
    coa_id = (SELECT id FROM acc_chart_of_accounts WHERE code = '1130')
WHERE code = 'BA-0002';

-- BA-DEMO-03 (Cash) → IDR, COA 1110 (Cash)
UPDATE bank_accounts SET currency_id = (SELECT id FROM master_currencies WHERE code = 'IDR'),
    coa_id = (SELECT id FROM acc_chart_of_accounts WHERE code = '1110')
WHERE code = 'BA-0003';

-- BA-DEMO-04 (Giro) → IDR, no COA
UPDATE bank_accounts SET currency_id = (SELECT id FROM master_currencies WHERE code = 'IDR'),
    coa_id = NULL
WHERE code = 'BA-0004';
