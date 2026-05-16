-- Development seeder master bank accounts.

SET @geo_jkt_pusat = 213; -- Kota Adm. Jakarta Pusat
SET @geo_jkt_selatan = 216; -- Kota Adm. Jakarta Selatan
SET @geo_jkt_timur = 217; -- Kota Adm. Jakarta Timur

SET @party_internal = (SELECT id FROM parties WHERE code = 'PRT-INTERNAL-01');
SET @party_supplier = (SELECT id FROM parties WHERE code = 'BP-DEV-SUP01');

SET @currency_idr = (SELECT id FROM master_currencies WHERE alias = 'IDR' LIMIT 1);
SET @currency_usd = (SELECT id FROM master_currencies WHERE alias = 'USD' LIMIT 1);
SET @currency_sgd = (SELECT id FROM master_currencies WHERE alias = 'SGD' LIMIT 1);

SET @coa_cash = (SELECT id FROM acc_chart_of_accounts WHERE code = '1110');
SET @coa_main_bank = (SELECT id FROM acc_chart_of_accounts WHERE code = '1120');
SET @coa_savings_bank = (SELECT id FROM acc_chart_of_accounts WHERE code = '1130');
SET @coa_usd_bank = (SELECT id FROM acc_chart_of_accounts WHERE code = '1140');
SET @coa_sgd_bank = (SELECT id FROM acc_chart_of_accounts WHERE code = '1150');

INSERT INTO bank_accounts (
    code,
    bank_name,
    branch,
    city_id,
    party_id,
    account_name,
    account_no,
    account_type,
    note,
    is_active,
    currency_id,
    coa_id,
    version,
    created_by_user_id,
    created_date,
    updated_by_user_id,
    updated_date
)
VALUES
    ('BA-DEMO-01', 'BCA', 'KCU Sudirman', @geo_jkt_pusat, @party_internal, 'PT Solusi Program Operasional', '1234567890', 'BANK_TRANSFER', 'Rekening operasional utama perusahaan IDR.', 1, @currency_idr, @coa_main_bank, 1, 1, NOW(), 1, NOW()),
    ('BA-DEMO-02', 'Mandiri', 'KC Gatot Subroto', @geo_jkt_selatan, @party_internal, 'PT Solusi Program Payroll', '9876543210', 'BANK_TRANSFER', 'Rekening payroll dan reimbursement IDR.', 1, @currency_idr, @coa_savings_bank, 1, 1, NOW(), 1, NOW()),
    ('BA-DEMO-03', 'Kas Pusat', 'Head Office', @geo_jkt_pusat, @party_internal, 'Kas Kantor Pusat', 'CASH-001', 'CASH', 'Kas fisik untuk petty cash dan pengeluaran kecil.', 1, @currency_idr, @coa_cash, 1, 1, NOW(), 1, NOW()),
    ('BA-DEMO-04', 'BCA', 'KC Pulogadung', @geo_jkt_timur, @party_supplier, 'PT Sumber Makmur', '1122334455', 'BANK_TRANSFER', 'Contoh rekening supplier non-internal untuk lookup party.', 1, @currency_idr, @coa_main_bank, 1, 1, NOW(), 1, NOW()),
    ('BA-DEMO-05', 'BCA', 'KCU Sudirman', @geo_jkt_pusat, @party_internal, 'PT Solusi Program USD Operating', '2233445566', 'BANK_TRANSFER', 'Rekening operasional USD untuk transaksi valas.', 1, @currency_usd, @coa_usd_bank, 1, 1, NOW(), 1, NOW()),
    ('BA-DEMO-06', 'DBS', 'Singapore Branch', @geo_jkt_pusat, @party_internal, 'PT Solusi Program SGD Reserve', 'SGD-778899', 'BANK_TRANSFER', 'Rekening cadangan SGD untuk transaksi regional.', 1, @currency_sgd, @coa_sgd_bank, 1, 1, NOW(), 1, NOW()),
    ('BA-DEMO-07', 'Mandiri', 'KC Gatot Subroto', @geo_jkt_selatan, @party_internal, 'PT Solusi Program Giro', 'GIRO-001', 'GIRO', 'Rekening giro IDR untuk pembayaran mundur.', 1, @currency_idr, @coa_savings_bank, 1, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE
    bank_name = VALUES(bank_name),
    branch = VALUES(branch),
    city_id = VALUES(city_id),
    party_id = VALUES(party_id),
    account_name = VALUES(account_name),
    account_no = VALUES(account_no),
    account_type = VALUES(account_type),
    note = VALUES(note),
    is_active = VALUES(is_active),
    currency_id = VALUES(currency_id),
    coa_id = VALUES(coa_id),
    updated_by_user_id = VALUES(updated_by_user_id),
    updated_date = VALUES(updated_date);
