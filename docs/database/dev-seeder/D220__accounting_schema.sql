-- Development seeder accounting schema mappings.
-- Updated for V56: account mappings moved from acc_accounting_schemas columns
-- to the acc_schema_lines table (schema_id, variable, account_id, position).
-- Only GOODS_RECEIPT has JournalVariable entries defined; other event types
-- are registered as schema headers without lines until their variables are added.

SET @coa_merchandise_inventory = (SELECT id FROM acc_chart_of_accounts WHERE code = '1310' AND is_active = 1 AND is_header = 0);
SET @coa_input_vat             = (SELECT id FROM acc_chart_of_accounts WHERE code = '1230' AND is_active = 1 AND is_header = 0);
SET @coa_grir_clearing         = (SELECT id FROM acc_chart_of_accounts WHERE code = '2120' AND is_active = 1 AND is_header = 0);

-- Replace seeded schema headers owned by this seeder.
-- acc_schema_lines rows cascade-delete with the parent schema row.
DELETE FROM acc_accounting_schemas
WHERE event_type IN (
    'GOODS_RECEIPT', 'VENDOR_BILL', 'VENDOR_PAYMENT',
    'CUSTOMER_INVOICE', 'GOODS_ISSUE', 'CUSTOMER_RECEIPT',
    'STOCK_ADJUSTMENT_IN', 'STOCK_ADJUSTMENT_OUT'
);

INSERT INTO acc_accounting_schemas (event_type, description, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('GOODS_RECEIPT',        'Receive inventory into stock before vendor billing.',               1, 1, 1, NOW(), 1, NOW()),
('VENDOR_BILL',          'Recognize supplier invoice against prior goods receipt clearing.', 1, 1, 1, NOW(), 1, NOW()),
('VENDOR_PAYMENT',       'Pay supplier liability from the main bank account.',               1, 1, 1, NOW(), 1, NOW()),
('CUSTOMER_INVOICE',     'Issue customer invoice for inventory sales.',                      1, 1, 1, NOW(), 1, NOW()),
('GOODS_ISSUE',          'Relieve inventory and recognize material cost of sales.',          1, 1, 1, NOW(), 1, NOW()),
('CUSTOMER_RECEIPT',     'Receive customer payment to the main bank account.',               1, 1, 1, NOW(), 1, NOW()),
('STOCK_ADJUSTMENT_IN',  'Increase stock from a positive inventory adjustment.',             1, 1, 1, NOW(), 1, NOW()),
('STOCK_ADJUSTMENT_OUT', 'Decrease stock from a negative inventory adjustment.',             1, 1, 1, NOW(), 1, NOW());

-- Schema lines for GOODS_RECEIPT (only event type with JournalVariable entries).
SET @schema_gr = (SELECT id FROM acc_accounting_schemas WHERE event_type = 'GOODS_RECEIPT');

INSERT INTO acc_schema_lines (schema_id, variable, account_id, position)
VALUES
(@schema_gr, 'GR_INVENTORY_AMT', @coa_merchandise_inventory, 'DEBIT'),
(@schema_gr, 'GR_TAX_AMT',       @coa_input_vat,             'DEBIT'),
(@schema_gr, 'GR_GRAND_TOTAL',   @coa_grir_clearing,         'CREDIT');

-- Validation queries for manual checks.
-- SELECT COUNT(*) FROM acc_fiscal_years WHERE code IN ('FY-2024', 'FY-2025'); -- Expected: 2
-- SELECT COUNT(*) FROM acc_accounting_periods WHERE code LIKE 'AP-2024-%' OR code LIKE 'AP-2025-%'; -- Expected: 24
-- SELECT level, COUNT(*) FROM acc_chart_of_accounts GROUP BY level ORDER BY level;
-- SELECT event_type FROM acc_accounting_schemas ORDER BY event_type; -- Expected: 8 rows
-- SELECT s.event_type, l.variable, l.position FROM acc_accounting_schemas s JOIN acc_schema_lines l ON l.schema_id = s.id; -- Expected: 3 rows for GOODS_RECEIPT
