-- V71__Add_Purchase_Return_Accounting_Schema.sql
-- Seed accounting schema for PURCHASE_RETURN event.

SET @schema_id = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'PURCHASE_RETURN' AND is_active = TRUE LIMIT 1);

INSERT INTO acc_accounting_schemas (event_type, description, is_active, created_by_user_id, created_date, version)
SELECT 'PURCHASE_RETURN', 'Purchase return journal posting schema', TRUE, 1, NOW(), 1
WHERE @schema_id IS NULL;

SET @schema_id = COALESCE(@schema_id, LAST_INSERT_ID());

DELETE FROM acc_schema_lines WHERE schema_id = @schema_id;

-- PR_GRIR_CLEARING_AMT -> 2120 (GR/IR Clearing) DEBIT
INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'PR_GRIR_CLEARING_AMT', id, 'DEBIT' FROM acc_chart_of_accounts WHERE code = '2120';

-- PR_INVENTORY_AMT -> 1310 (Merchandise Inventory) CREDIT
INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
SELECT @schema_id, 'PR_INVENTORY_AMT', id, 'CREDIT' FROM acc_chart_of_accounts WHERE code = '1310';
