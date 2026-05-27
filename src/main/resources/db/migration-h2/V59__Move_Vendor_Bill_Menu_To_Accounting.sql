-- V59: Move Vendor Bill menu under Finance & Accounting > Account Payable

UPDATE permission_groups
SET breadcrumb_id = 'Keuangan & Akuntansi > Hutang Usaha > Tagihan Vendor',
    breadcrumb_en = 'Finance & Accounting > Account Payable > Vendor Bill',
    sort_order = 350
WHERE code = 'AP-01';
