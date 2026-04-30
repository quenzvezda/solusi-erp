-- Development seeder master bank accounts.

SET @geo_jkt_pusat = 213; -- Kota Adm. Jakarta Pusat
SET @geo_jkt_selatan = 216; -- Kota Adm. Jakarta Selatan
SET @geo_jkt_timur = 217; -- Kota Adm. Jakarta Timur

SET @party_internal = (SELECT id FROM parties WHERE code = 'PRT-INTERNAL-01');
SET @party_supplier = (SELECT id FROM parties WHERE code = 'BP-DEV-SUP01');

DELETE FROM bank_accounts
WHERE code IN ('BA-DEMO-01', 'BA-DEMO-02', 'BA-DEMO-03', 'BA-DEMO-04');

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
    version,
    created_by_user_id,
    created_date,
    updated_by_user_id,
    updated_date
)
VALUES
    ('BA-DEMO-01', 'BCA', 'KCU Sudirman', @geo_jkt_pusat, @party_internal, 'PT Solusi Program Operasional', '1234567890', 'BANK', 'Rekening operasional utama perusahaan.', 1, 1, 1, NOW(), 1, NOW()),
    ('BA-DEMO-02', 'Mandiri', 'KC Gatot Subroto', @geo_jkt_selatan, @party_internal, 'PT Solusi Program Payroll', '9876543210', 'BANK', 'Rekening payroll dan reimbursement.', 1, 1, 1, NOW(), 1, NOW()),
    ('BA-DEMO-03', 'Kas Pusat', 'Head Office', @geo_jkt_pusat, @party_internal, 'Kas Kantor Pusat', 'CASH-001', 'CASH', 'Kas fisik untuk petty cash dan pengeluaran kecil.', 1, 1, 1, NOW(), 1, NOW()),
    ('BA-DEMO-04', 'BCA', 'KC Pulogadung', @geo_jkt_timur, @party_supplier, 'PT Sumber Makmur', '1122334455', 'BANK', 'Contoh rekening holder non-internal untuk lookup party.', 1, 1, 1, NOW(), 1, NOW());
