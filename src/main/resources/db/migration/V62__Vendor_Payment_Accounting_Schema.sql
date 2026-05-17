-- V62__Vendor_Payment_Accounting_Schema.sql
-- Seed accounting schema for VENDOR_PAYMENT event

SET @schema_id = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'VENDOR_PAYMENT' AND is_active = TRUE LIMIT 1);

INSERT INTO acc_accounting_schemas (event_type, description, is_active, created_by_user_id, created_date, version)
SELECT 'VENDOR_PAYMENT', 'Vendor payment journal posting schema', TRUE, 1, NOW(), 1
WHERE @schema_id IS NULL;

SET @schema_id = COALESCE(@schema_id, LAST_INSERT_ID());

DELETE FROM acc_schema_lines WHERE schema_id = @schema_id;

-- VP_AP_AMT → 2110 (Accounts Payable) DEBIT
INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'VP_AP_AMT', id, 'DEBIT' FROM acc_chart_of_accounts WHERE code = '2110';

-- VP_BANK_OUT_AMT → 1120 (Bank) CREDIT (default, can be overridden by bank account COA)
INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'VP_BANK_OUT_AMT', id, 'CREDIT' FROM acc_chart_of_accounts WHERE code = '1120';

-- VP_FX_LOSS_AMT → 5140 (FX Loss) DEBIT
INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'VP_FX_LOSS_AMT', id, 'DEBIT' FROM acc_chart_of_accounts WHERE code = '5140';

-- VP_FX_GAIN_AMT → 4240 (FX Gain) CREDIT
INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'VP_FX_GAIN_AMT', id, 'CREDIT' FROM acc_chart_of_accounts WHERE code = '4240';
