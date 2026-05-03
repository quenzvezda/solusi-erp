-- V56__Refactor_Schema_To_Dynamic_Lines.sql

-- 1. Create lines table
CREATE TABLE acc_schema_lines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schema_id BIGINT NOT NULL,
    variable VARCHAR(50) NOT NULL,
    account_id BIGINT NOT NULL,
    position VARCHAR(10) NOT NULL,
    CONSTRAINT fk_acc_schema_lines_schema FOREIGN KEY (schema_id) REFERENCES acc_accounting_schemas(id) ON DELETE CASCADE,
    CONSTRAINT fk_acc_schema_lines_account FOREIGN KEY (account_id) REFERENCES acc_chart_of_accounts(id)
);

-- 2. Migrate existing hardcoded accounts to lines (if any exist)
INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT id, 'GR_INVENTORY_AMT', debit_account_id, 'DEBIT' 
FROM acc_accounting_schemas WHERE debit_account_id IS NOT NULL AND event_type = 'GOODS_RECEIPT';

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT id, 'GR_TAX_AMT', tax_account_id, 'DEBIT' 
FROM acc_accounting_schemas WHERE tax_account_id IS NOT NULL AND event_type = 'GOODS_RECEIPT';

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT id, 'GR_GRAND_TOTAL', credit_account_id, 'CREDIT' 
FROM acc_accounting_schemas WHERE credit_account_id IS NOT NULL AND event_type = 'GOODS_RECEIPT';

-- 3. Drop old columns
ALTER TABLE acc_accounting_schemas
DROP FOREIGN KEY fk_schema_debit,
DROP FOREIGN KEY fk_schema_credit,
DROP FOREIGN KEY fk_schema_tax;

ALTER TABLE acc_accounting_schemas
DROP COLUMN debit_account_id,
DROP COLUMN credit_account_id,
DROP COLUMN tax_account_id;