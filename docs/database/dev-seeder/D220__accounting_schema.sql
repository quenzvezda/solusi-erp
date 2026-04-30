-- Development seeder accounting schema mappings.

SET @coa_trade_receivable = (SELECT id FROM acc_chart_of_accounts WHERE code = '1210' AND is_active = 1 AND is_header = 0);
SET @coa_merchandise_inventory = (SELECT id FROM acc_chart_of_accounts WHERE code = '1310' AND is_active = 1 AND is_header = 0);
SET @coa_accounts_payable = (SELECT id FROM acc_chart_of_accounts WHERE code = '2110' AND is_active = 1 AND is_header = 0);
SET @coa_grir_clearing = (SELECT id FROM acc_chart_of_accounts WHERE code = '2120' AND is_active = 1 AND is_header = 0);
SET @coa_main_bank = (SELECT id FROM acc_chart_of_accounts WHERE code = '1120' AND is_active = 1 AND is_header = 0);
SET @coa_product_sales = (SELECT id FROM acc_chart_of_accounts WHERE code = '4110' AND is_active = 1 AND is_header = 0);
SET @coa_cogs_material = (SELECT id FROM acc_chart_of_accounts WHERE code = '5110' AND is_active = 1 AND is_header = 0);
SET @coa_inventory_adjustment_gain = (SELECT id FROM acc_chart_of_accounts WHERE code = '4230' AND is_active = 1 AND is_header = 0);
SET @coa_inventory_adjustment_loss = (SELECT id FROM acc_chart_of_accounts WHERE code = '5130' AND is_active = 1 AND is_header = 0);

-- Upsert the dev schema mappings owned by this seeder.
INSERT INTO acc_accounting_schemas (event_type, description, debit_account_id, credit_account_id, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('GOODS_RECEIPT',        'Receive inventory into stock before vendor billing.',               @coa_merchandise_inventory,     @coa_grir_clearing,           1, 1, 1, NOW(), 1, NOW()),
('VENDOR_BILL',          'Recognize supplier invoice against prior goods receipt clearing.', @coa_grir_clearing,             @coa_accounts_payable,        1, 1, 1, NOW(), 1, NOW()),
('VENDOR_PAYMENT',       'Pay supplier liability from the main bank account.',               @coa_accounts_payable,          @coa_main_bank,               1, 1, 1, NOW(), 1, NOW()),
('CUSTOMER_INVOICE',     'Issue customer invoice for inventory sales.',                      @coa_trade_receivable,          @coa_product_sales,           1, 1, 1, NOW(), 1, NOW()),
('GOODS_ISSUE',          'Relieve inventory and recognize material cost of sales.',          @coa_cogs_material,             @coa_merchandise_inventory,   1, 1, 1, NOW(), 1, NOW()),
('CUSTOMER_RECEIPT',     'Receive customer payment to the main bank account.',               @coa_main_bank,                 @coa_trade_receivable,        1, 1, 1, NOW(), 1, NOW()),
('STOCK_ADJUSTMENT_IN',  'Increase stock from a positive inventory adjustment.',             @coa_merchandise_inventory,     @coa_inventory_adjustment_gain, 1, 1, 1, NOW(), 1, NOW()),
('STOCK_ADJUSTMENT_OUT', 'Decrease stock from a negative inventory adjustment.',             @coa_inventory_adjustment_loss, @coa_merchandise_inventory,   1, 1, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    debit_account_id = VALUES(debit_account_id),
    credit_account_id = VALUES(credit_account_id),
    is_active = VALUES(is_active),
    version = VALUES(version),
    updated_by_user_id = VALUES(updated_by_user_id),
    updated_date = VALUES(updated_date);

-- Validation queries for manual checks.
-- SELECT COUNT(*) FROM acc_fiscal_years WHERE code IN ('FY-2024', 'FY-2025'); -- Expected: 2
-- SELECT COUNT(*) FROM acc_accounting_periods WHERE code LIKE 'AP-2024-%' OR code LIKE 'AP-2025-%'; -- Expected: 24
-- SELECT level, COUNT(*) FROM acc_chart_of_accounts GROUP BY level ORDER BY level;
-- SELECT event_type, debit_account_id, credit_account_id FROM acc_accounting_schemas ORDER BY event_type;
