-- Development seeder accounting chart of accounts.

-- Remove seeded schema rows that still reference the dev COA hierarchy before replacing it.
DELETE schema_mapping
FROM acc_accounting_schemas schema_mapping
INNER JOIN acc_chart_of_accounts seeded_coa
    ON seeded_coa.id = schema_mapping.debit_account_id
    OR seeded_coa.id = schema_mapping.credit_account_id
WHERE seeded_coa.code REGEXP '^[1-5][0-9]{3}$';

-- Replace the dev COA hierarchy owned by this seeder.
DELETE FROM acc_chart_of_accounts
WHERE code REGEXP '^[1-5][0-9]{3}$';

-- Level 1: root headers.
INSERT INTO acc_chart_of_accounts (code, name, account_type, normal_balance, parent_id, level, is_header, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('1000', 'ASSETS',      'ASSET',     'DEBIT',  NULL, 1, 1, 'Root group for asset accounts.',      1, 1, 1, NOW(), 1, NOW()),
('2000', 'LIABILITIES', 'LIABILITY', 'CREDIT', NULL, 1, 1, 'Root group for liability accounts.',  1, 1, 1, NOW(), 1, NOW()),
('3000', 'EQUITY',      'EQUITY',    'CREDIT', NULL, 1, 1, 'Root group for equity accounts.',     1, 1, 1, NOW(), 1, NOW()),
('4000', 'REVENUE',     'REVENUE',   'CREDIT', NULL, 1, 1, 'Root group for revenue accounts.',    1, 1, 1, NOW(), 1, NOW()),
('5000', 'EXPENSES',    'EXPENSE',   'DEBIT',  NULL, 1, 1, 'Root group for expense accounts.',    1, 1, 1, NOW(), 1, NOW());

SET @coa_assets = (SELECT id FROM acc_chart_of_accounts WHERE code = '1000');
SET @coa_liabilities = (SELECT id FROM acc_chart_of_accounts WHERE code = '2000');
SET @coa_equity = (SELECT id FROM acc_chart_of_accounts WHERE code = '3000');
SET @coa_revenue = (SELECT id FROM acc_chart_of_accounts WHERE code = '4000');
SET @coa_expenses = (SELECT id FROM acc_chart_of_accounts WHERE code = '5000');

-- Level 2: reporting groups.
INSERT INTO acc_chart_of_accounts (code, name, account_type, normal_balance, parent_id, level, is_header, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('1100', 'CASH AND CASH EQUIVALENTS',        'ASSET',     'DEBIT',  @coa_assets,      2, 1, 'Petty cash and operating bank balances.',           1, 1, 1, NOW(), 1, NOW()),
('1200', 'RECEIVABLES AND ADVANCES',         'ASSET',     'DEBIT',  @coa_assets,      2, 1, 'Trade receivables and short-term advances.',        1, 1, 1, NOW(), 1, NOW()),
('1300', 'INVENTORY AND PREPAID ASSETS',     'ASSET',     'DEBIT',  @coa_assets,      2, 1, 'Inventory positions and prepaid operating assets.', 1, 1, 1, NOW(), 1, NOW()),
('1400', 'PROPERTY PLANT AND EQUIPMENT',     'ASSET',     'DEBIT',  @coa_assets,      2, 1, 'Long-lived fixed assets and contra assets.',        1, 1, 1, NOW(), 1, NOW()),
('2100', 'PAYABLES AND ACCRUALS',            'LIABILITY', 'CREDIT', @coa_liabilities, 2, 1, 'Trade payables, GR/IR, taxes, and accruals.',       1, 1, 1, NOW(), 1, NOW()),
('2200', 'FINANCING LIABILITIES',            'LIABILITY', 'CREDIT', @coa_liabilities, 2, 1, 'Short-term and long-term financing obligations.',   1, 1, 1, NOW(), 1, NOW()),
('3100', 'CONTRIBUTED CAPITAL',              'EQUITY',    'CREDIT', @coa_equity,      2, 1, 'Capital injected by owners.',                       1, 1, 1, NOW(), 1, NOW()),
('3200', 'RETAINED AND CURRENT EARNINGS',    'EQUITY',    'CREDIT', @coa_equity,      2, 1, 'Accumulated earnings and current year result.',     1, 1, 1, NOW(), 1, NOW()),
('4100', 'OPERATING REVENUE',                'REVENUE',   'CREDIT', @coa_revenue,     2, 1, 'Product and service revenue accounts.',             1, 1, 1, NOW(), 1, NOW()),
('4200', 'SALES CONTRA AND INVENTORY GAINS', 'REVENUE',   'CREDIT', @coa_revenue,     2, 1, 'Contra-sales and stock adjustment gains.',          1, 1, 1, NOW(), 1, NOW()),
('5100', 'COST OF SALES AND STOCK LOSSES',   'EXPENSE',   'DEBIT',  @coa_expenses,    2, 1, 'COGS and stock shrinkage accounts.',                1, 1, 1, NOW(), 1, NOW()),
('5200', 'OPERATING EXPENSES',               'EXPENSE',   'DEBIT',  @coa_expenses,    2, 1, 'General operating expense accounts.',               1, 1, 1, NOW(), 1, NOW());

SET @coa_cash = (SELECT id FROM acc_chart_of_accounts WHERE code = '1100');
SET @coa_receivables = (SELECT id FROM acc_chart_of_accounts WHERE code = '1200');
SET @coa_inventory = (SELECT id FROM acc_chart_of_accounts WHERE code = '1300');
SET @coa_fixed_assets = (SELECT id FROM acc_chart_of_accounts WHERE code = '1400');
SET @coa_payables = (SELECT id FROM acc_chart_of_accounts WHERE code = '2100');
SET @coa_financing = (SELECT id FROM acc_chart_of_accounts WHERE code = '2200');
SET @coa_capital = (SELECT id FROM acc_chart_of_accounts WHERE code = '3100');
SET @coa_earnings = (SELECT id FROM acc_chart_of_accounts WHERE code = '3200');
SET @coa_operating_revenue = (SELECT id FROM acc_chart_of_accounts WHERE code = '4100');
SET @coa_contra_sales = (SELECT id FROM acc_chart_of_accounts WHERE code = '4200');
SET @coa_cost_of_sales = (SELECT id FROM acc_chart_of_accounts WHERE code = '5100');
SET @coa_operating_expense = (SELECT id FROM acc_chart_of_accounts WHERE code = '5200');

-- Level 3: postable detail accounts for operational testing.
INSERT INTO acc_chart_of_accounts (code, name, account_type, normal_balance, parent_id, level, is_header, note, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('1110', 'Cash on Hand',                       'ASSET',     'DEBIT',  @coa_cash,              3, 0, 'Petty cash and manual cash receipts.',                      1, 1, 1, NOW(), 1, NOW()),
('1120', 'Main Bank Account',                  'ASSET',     'DEBIT',  @coa_cash,              3, 0, 'Primary operating bank account for disbursement and AR.',  1, 1, 1, NOW(), 1, NOW()),
('1130', 'Backup/Savings Bank Account',        'ASSET',     'DEBIT',  @coa_cash,              3, 0, 'Secondary reserve bank account.',                          1, 1, 1, NOW(), 1, NOW()),
('1210', 'Trade Receivable',                   'ASSET',     'DEBIT',  @coa_receivables,       3, 0, 'Outstanding receivables from customer invoices.',         1, 1, 1, NOW(), 1, NOW()),
('1220', 'Employee and Other Receivable',      'ASSET',     'DEBIT',  @coa_receivables,       3, 0, 'Miscellaneous short-term receivables.',                    1, 1, 1, NOW(), 1, NOW()),
('1310', 'Merchandise Inventory',              'ASSET',     'DEBIT',  @coa_inventory,         3, 0, 'Inventory held for resale and operational stock.',         1, 1, 1, NOW(), 1, NOW()),
('1320', 'Prepaid Expenses',                   'ASSET',     'DEBIT',  @coa_inventory,         3, 0, 'Prepaid rent, insurance, and service contracts.',         1, 1, 1, NOW(), 1, NOW()),
('1410', 'Land/Building',                      'ASSET',     'DEBIT',  @coa_fixed_assets,      3, 0, 'Office, warehouse, and building ownership cost.',         1, 1, 1, NOW(), 1, NOW()),
('1420', 'Equipment',                          'ASSET',     'DEBIT',  @coa_fixed_assets,      3, 0, 'Machines, laptops, and warehouse equipment.',             1, 1, 1, NOW(), 1, NOW()),
('1430', 'Vehicles',                           'ASSET',     'DEBIT',  @coa_fixed_assets,      3, 0, 'Operational vehicles and delivery fleet.',                1, 1, 1, NOW(), 1, NOW()),
('1440', 'Accumulated Depreciation - Building','ASSET',     'CREDIT', @coa_fixed_assets,      3, 0, 'Contra asset for land/building depreciation.',            1, 1, 1, NOW(), 1, NOW()),
('1450', 'Accumulated Depreciation - Equipment','ASSET',    'CREDIT', @coa_fixed_assets,      3, 0, 'Contra asset for equipment depreciation.',                1, 1, 1, NOW(), 1, NOW()),
('1460', 'Accumulated Depreciation - Vehicles','ASSET',     'CREDIT', @coa_fixed_assets,      3, 0, 'Contra asset for vehicle depreciation.',                  1, 1, 1, NOW(), 1, NOW()),
('2110', 'Accounts Payable',                   'LIABILITY', 'CREDIT', @coa_payables,          3, 0, 'Supplier liabilities from approved vendor bills.',        1, 1, 1, NOW(), 1, NOW()),
('2120', 'GR/IR Clearing',                     'LIABILITY', 'CREDIT', @coa_payables,          3, 0, 'Temporary clearing between goods receipt and bill.',      1, 1, 1, NOW(), 1, NOW()),
('2130', 'Tax Payable',                        'LIABILITY', 'CREDIT', @coa_payables,          3, 0, 'VAT and other statutory tax obligations.',                1, 1, 1, NOW(), 1, NOW()),
('2140', 'Accrued Liabilities',                'LIABILITY', 'CREDIT', @coa_payables,          3, 0, 'Accrued payroll and operating liabilities.',              1, 1, 1, NOW(), 1, NOW()),
('2210', 'Short-term Loan',                    'LIABILITY', 'CREDIT', @coa_financing,         3, 0, 'Bank or shareholder loans due within one year.',          1, 1, 1, NOW(), 1, NOW()),
('2220', 'Long-term Loan',                     'LIABILITY', 'CREDIT', @coa_financing,         3, 0, 'Long-term financing obligations.',                        1, 1, 1, NOW(), 1, NOW()),
('3110', 'Paid-in Capital',                    'EQUITY',    'CREDIT', @coa_capital,           3, 0, 'Owner invested capital.',                                1, 1, 1, NOW(), 1, NOW()),
('3210', 'Retained Earnings',                  'EQUITY',    'CREDIT', @coa_earnings,          3, 0, 'Accumulated profit retained in the business.',           1, 1, 1, NOW(), 1, NOW()),
('3220', 'Current Year Profit/Loss',           'EQUITY',    'CREDIT', @coa_earnings,          3, 0, 'Current year closing result before appropriation.',      1, 1, 1, NOW(), 1, NOW()),
('4110', 'Product Sales',                      'REVENUE',   'CREDIT', @coa_operating_revenue, 3, 0, 'Revenue from inventory-based customer sales.',           1, 1, 1, NOW(), 1, NOW()),
('4120', 'Service Revenue',                    'REVENUE',   'CREDIT', @coa_operating_revenue, 3, 0, 'Revenue from non-inventory services.',                   1, 1, 1, NOW(), 1, NOW()),
('4210', 'Sales Discount',                     'REVENUE',   'DEBIT',  @coa_contra_sales,      3, 0, 'Contra revenue for discounts granted to customers.',     1, 1, 1, NOW(), 1, NOW()),
('4220', 'Sales Return',                       'REVENUE',   'DEBIT',  @coa_contra_sales,      3, 0, 'Contra revenue for returned sold goods.',                1, 1, 1, NOW(), 1, NOW()),
('4230', 'Inventory Adjustment Gain',          'REVENUE',   'CREDIT', @coa_contra_sales,      3, 0, 'Gain recognized from positive stock adjustments.',       1, 1, 1, NOW(), 1, NOW()),
('5110', 'COGS - Material',                    'EXPENSE',   'DEBIT',  @coa_cost_of_sales,     3, 0, 'Material cost recognized on goods issue.',               1, 1, 1, NOW(), 1, NOW()),
('5120', 'COGS - Labor',                       'EXPENSE',   'DEBIT',  @coa_cost_of_sales,     3, 0, 'Labor component of cost of goods sold.',                 1, 1, 1, NOW(), 1, NOW()),
('5130', 'Inventory Adjustment Loss',          'EXPENSE',   'DEBIT',  @coa_cost_of_sales,     3, 0, 'Loss recognized from negative stock adjustments.',       1, 1, 1, NOW(), 1, NOW()),
('5210', 'Salaries and Wages',                 'EXPENSE',   'DEBIT',  @coa_operating_expense, 3, 0, 'Payroll expense for employees.',                         1, 1, 1, NOW(), 1, NOW()),
('5220', 'Utilities',                          'EXPENSE',   'DEBIT',  @coa_operating_expense, 3, 0, 'Electricity, water, internet, and telephone.',          1, 1, 1, NOW(), 1, NOW()),
('5230', 'Depreciation Expense',               'EXPENSE',   'DEBIT',  @coa_operating_expense, 3, 0, 'Periodic depreciation expense.',                         1, 1, 1, NOW(), 1, NOW()),
('5240', 'Office Supplies',                    'EXPENSE',   'DEBIT',  @coa_operating_expense, 3, 0, 'Stationery and office consumables.',                     1, 1, 1, NOW(), 1, NOW()),
('5250', 'Rent Expense',                       'EXPENSE',   'DEBIT',  @coa_operating_expense, 3, 0, 'Office and warehouse rent expense.',                     1, 1, 1, NOW(), 1, NOW()),
('5260', 'Transportation & Logistics',         'EXPENSE',   'DEBIT',  @coa_operating_expense, 3, 0, 'Freight, courier, and distribution costs.',             1, 1, 1, NOW(), 1, NOW()),
('5270', 'Marketing & Advertising',            'EXPENSE',   'DEBIT',  @coa_operating_expense, 3, 0, 'Branding, promotions, and campaign cost.',              1, 1, 1, NOW(), 1, NOW()),
('5280', 'Tax Expense',                        'EXPENSE',   'DEBIT',  @coa_operating_expense, 3, 0, 'Non-creditable taxes and income tax expense.',          1, 1, 1, NOW(), 1, NOW());
