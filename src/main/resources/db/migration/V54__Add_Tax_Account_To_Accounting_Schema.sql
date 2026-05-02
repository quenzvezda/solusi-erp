ALTER TABLE acc_accounting_schemas
ADD COLUMN tax_account_id BIGINT NULL AFTER credit_account_id;

ALTER TABLE acc_accounting_schemas
ADD CONSTRAINT fk_schema_tax FOREIGN KEY (tax_account_id) REFERENCES acc_chart_of_accounts(id);
