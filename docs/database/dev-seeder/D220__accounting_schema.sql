-- Development seeder accounting schema mappings.
-- Updated for V56: account mappings moved from acc_accounting_schemas columns
-- to the acc_schema_lines table (schema_id, variable, account_id, position).
-- This seeder is idempotent: rerunning it refreshes headers and seeded lines
-- for the targeted event types without duplicating data.

SET @seeded_updated_by = 1;

SET @coa_main_bank             = (SELECT id FROM acc_chart_of_accounts WHERE code = '1120' AND is_active = 1 AND is_header = 0);
SET @coa_trade_receivable      = (SELECT id FROM acc_chart_of_accounts WHERE code = '1210' AND is_active = 1 AND is_header = 0);
SET @coa_input_vat             = (SELECT id FROM acc_chart_of_accounts WHERE code = '1230' AND is_active = 1 AND is_header = 0);
SET @coa_merchandise_inventory = (SELECT id FROM acc_chart_of_accounts WHERE code = '1310' AND is_active = 1 AND is_header = 0);
SET @coa_accounts_payable      = (SELECT id FROM acc_chart_of_accounts WHERE code = '2110' AND is_active = 1 AND is_header = 0);
SET @coa_grir_clearing         = (SELECT id FROM acc_chart_of_accounts WHERE code = '2120' AND is_active = 1 AND is_header = 0);
SET @coa_tax_payable           = (SELECT id FROM acc_chart_of_accounts WHERE code = '2130' AND is_active = 1 AND is_header = 0);
SET @coa_product_sales         = (SELECT id FROM acc_chart_of_accounts WHERE code = '4110' AND is_active = 1 AND is_header = 0);
SET @coa_inventory_gain        = (SELECT id FROM acc_chart_of_accounts WHERE code = '4230' AND is_active = 1 AND is_header = 0);
SET @coa_fx_gain               = (SELECT id FROM acc_chart_of_accounts WHERE code = '4240' AND is_active = 1 AND is_header = 0);
SET @coa_cogs_material         = (SELECT id FROM acc_chart_of_accounts WHERE code = '5110' AND is_active = 1 AND is_header = 0);
SET @coa_inventory_loss        = (SELECT id FROM acc_chart_of_accounts WHERE code = '5130' AND is_active = 1 AND is_header = 0);
SET @coa_fx_loss               = (SELECT id FROM acc_chart_of_accounts WHERE code = '5140' AND is_active = 1 AND is_header = 0);

INSERT INTO acc_accounting_schemas (
    event_type,
    description,
    is_active,
    version,
    created_by_user_id,
    created_date,
    updated_by_user_id,
    updated_date
)
VALUES
('GOODS_RECEIPT',        'Receive inventory into stock before vendor billing.',               1, 1, 1, NOW(), @seeded_updated_by, NOW()),
('VENDOR_BILL',          'Recognize supplier invoice against prior goods receipt clearing.', 1, 1, 1, NOW(), @seeded_updated_by, NOW()),
('VENDOR_PAYMENT',       'Pay supplier liability from the main bank account.',               1, 1, 1, NOW(), @seeded_updated_by, NOW()),
('CUSTOMER_INVOICE',     'Issue customer invoice for inventory sales.',                      1, 1, 1, NOW(), @seeded_updated_by, NOW()),
('GOODS_ISSUE',          'Relieve inventory and recognize material cost of sales.',          1, 1, 1, NOW(), @seeded_updated_by, NOW()),
('PURCHASE_RETURN',      'Reverse inventory and goods receipt clearing for supplier returns.', 1, 1, 1, NOW(), @seeded_updated_by, NOW()),
('CUSTOMER_RECEIPT',     'Receive customer payment to the main bank account.',               1, 1, 1, NOW(), @seeded_updated_by, NOW()),
('STOCK_ADJUSTMENT_IN',  'Increase stock from a positive inventory adjustment.',             1, 1, 1, NOW(), @seeded_updated_by, NOW()),
('STOCK_ADJUSTMENT_OUT', 'Decrease stock from a negative inventory adjustment.',             1, 1, 1, NOW(), @seeded_updated_by, NOW())
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    is_active = VALUES(is_active),
    updated_by_user_id = VALUES(updated_by_user_id),
    updated_date = VALUES(updated_date);

DELETE line
FROM acc_schema_lines line
JOIN acc_accounting_schemas schema_header ON schema_header.id = line.schema_id
WHERE schema_header.event_type IN (
    'GOODS_RECEIPT',
    'VENDOR_BILL',
    'VENDOR_PAYMENT',
    'CUSTOMER_INVOICE',
    'GOODS_ISSUE',
    'PURCHASE_RETURN',
    'CUSTOMER_RECEIPT',
    'STOCK_ADJUSTMENT_IN',
    'STOCK_ADJUSTMENT_OUT'
);

SET @schema_gr  = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'GOODS_RECEIPT');
SET @schema_vb  = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'VENDOR_BILL');
SET @schema_vp  = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'VENDOR_PAYMENT');
SET @schema_ci  = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'CUSTOMER_INVOICE');
SET @schema_gi  = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'GOODS_ISSUE');
SET @schema_prt = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'PURCHASE_RETURN');
SET @schema_cr  = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'CUSTOMER_RECEIPT');
SET @schema_sai = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'STOCK_ADJUSTMENT_IN');
SET @schema_sao = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'STOCK_ADJUSTMENT_OUT');

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
VALUES
(@schema_gr,  'GR_INVENTORY_AMT',      @coa_merchandise_inventory, 'DEBIT'),
(@schema_gr,  'GR_TAX_AMT',            @coa_input_vat,             'DEBIT'),
(@schema_gr,  'GR_GRAND_TOTAL',        @coa_grir_clearing,         'CREDIT'),
(@schema_vb,  'VB_GRIR_CLEARING_AMT',  @coa_grir_clearing,         'DEBIT'),
(@schema_vb,  'VB_TAX_AMT',            @coa_input_vat,             'DEBIT'),
(@schema_vb,  'VB_FX_LOSS_AMT',        @coa_fx_loss,               'DEBIT'),
(@schema_vb,  'VB_AP_TOTAL',           @coa_accounts_payable,      'CREDIT'),
(@schema_vb,  'VB_FX_GAIN_AMT',        @coa_fx_gain,               'CREDIT'),
(@schema_vp,  'VP_AP_AMT',             @coa_accounts_payable,      'DEBIT'),
(@schema_vp,  'VP_BANK_OUT_AMT',       @coa_main_bank,             'CREDIT'),
(@schema_ci,  'CI_AR_AMT',             @coa_trade_receivable,      'DEBIT'),
(@schema_ci,  'CI_REVENUE_AMT',        @coa_product_sales,         'CREDIT'),
(@schema_ci,  'CI_TAX_AMT',            @coa_tax_payable,           'CREDIT'),
(@schema_gi,  'GI_COGS_AMT',           @coa_cogs_material,         'DEBIT'),
(@schema_gi,  'GI_INVENTORY_AMT',      @coa_merchandise_inventory, 'CREDIT'),
(@schema_prt, 'PR_GRIR_CLEARING_AMT',  @coa_grir_clearing,         'DEBIT'),
(@schema_prt, 'PR_INVENTORY_AMT',      @coa_merchandise_inventory, 'CREDIT'),
(@schema_cr,  'CR_BANK_IN_AMT',        @coa_main_bank,             'DEBIT'),
(@schema_cr,  'CR_AR_AMT',             @coa_trade_receivable,      'CREDIT'),
(@schema_sai, 'SAI_INVENTORY_AMT',     @coa_merchandise_inventory, 'DEBIT'),
(@schema_sai, 'SAI_GAIN_AMT',          @coa_inventory_gain,        'CREDIT'),
(@schema_sao, 'SAO_LOSS_AMT',          @coa_inventory_loss,        'DEBIT'),
(@schema_sao, 'SAO_INVENTORY_AMT',     @coa_merchandise_inventory, 'CREDIT');

-- Validation queries for manual checks.
-- SELECT event_type, COUNT(*) AS header_count FROM acc_accounting_schemas WHERE event_type IN ('GOODS_RECEIPT','VENDOR_BILL','VENDOR_PAYMENT','CUSTOMER_INVOICE','GOODS_ISSUE','PURCHASE_RETURN','CUSTOMER_RECEIPT','STOCK_ADJUSTMENT_IN','STOCK_ADJUSTMENT_OUT') GROUP BY event_type;
-- SELECT s.event_type, COUNT(l.id) AS line_count FROM acc_accounting_schemas s LEFT JOIN acc_schema_lines l ON l.schema_id = s.id WHERE s.event_type IN ('GOODS_RECEIPT','VENDOR_BILL','VENDOR_PAYMENT','CUSTOMER_INVOICE','GOODS_ISSUE','PURCHASE_RETURN','CUSTOMER_RECEIPT','STOCK_ADJUSTMENT_IN','STOCK_ADJUSTMENT_OUT') GROUP BY s.event_type ORDER BY s.event_type; -- Expected: 3,5,2,3,2,2,2,2,2
