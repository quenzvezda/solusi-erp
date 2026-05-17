-- V20: Add Icon Class to Permission Groups & Refactor Menu Structure
-- Standard: AGENTS.md Section 5

ALTER TABLE permission_groups 
    ADD COLUMN icon_class VARCHAR(50) DEFAULT 'ti-circle';

-- Seed Icons for existing groups
UPDATE permission_groups SET icon_class = 'ti-users' WHERE code = 'SEC-01';
UPDATE permission_groups SET icon_class = 'ti-shield-lock' WHERE code = 'SEC-02';
UPDATE permission_groups SET icon_class = 'ti-key' WHERE code = 'SEC-03';
UPDATE permission_groups SET icon_class = 'ti-list-details' WHERE code = 'SEC-04';
UPDATE permission_groups SET icon_class = 'ti-package' WHERE code = 'INV-01';
UPDATE permission_groups SET icon_class = 'ti-category' WHERE code = 'INV-02';
UPDATE permission_groups SET icon_class = 'ti-tags' WHERE code = 'INV-03';
UPDATE permission_groups SET icon_class = 'ti-scale' WHERE code = 'INV-04';
UPDATE permission_groups SET icon_class = 'ti-building-skyscraper' WHERE code = 'MST-01';
UPDATE permission_groups SET icon_class = 'ti-map-pin' WHERE code = 'MST-02';
UPDATE permission_groups SET icon_class = 'ti-building-bank' WHERE code = 'MST-03';
UPDATE permission_groups SET icon_class = 'ti-receipt-tax' WHERE code = 'MST-04';
UPDATE permission_groups SET icon_class = 'ti-currency-dollar' WHERE code = 'MST-05';
UPDATE permission_groups SET icon_class = 'ti-user-check' WHERE code = 'MST-06';

-- Refactor Breadcrumb Structure (Company Admin & Grandchild structure)
UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Keamanan > Pengguna', breadcrumb_en = 'Company Admin > Security > Users' WHERE code = 'SEC-01';
UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Keamanan > Peran', breadcrumb_en = 'Company Admin > Security > Roles' WHERE code = 'SEC-02';
UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Keamanan > Otoritas', breadcrumb_en = 'Company Admin > Security > Permissions' WHERE code = 'SEC-03';
UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Keamanan > Grup Menu', breadcrumb_en = 'Company Admin > Security > Menu Groups' WHERE code = 'SEC-04';

UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Data Master > Business Partner', breadcrumb_en = 'Company Admin > Master Data > Business Partners' WHERE code = 'MST-01';
UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Data Master > Data Geografis', breadcrumb_en = 'Company Admin > Master Data > Geographic Data' WHERE code = 'MST-02';
UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Data Master > Tipe Peran Partner', breadcrumb_en = 'Company Admin > Master Data > Partner Role Types' WHERE code = 'MST-06';

UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Pengaturan Keuangan > Pajak', breadcrumb_en = 'Company Admin > Finance Settings > Taxes' WHERE code = 'MST-04';
UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Pengaturan Keuangan > Mata Uang', breadcrumb_en = 'Company Admin > Finance Settings > Currencies' WHERE code = 'MST-05';
UPDATE permission_groups SET breadcrumb_id = 'Admin Perusahaan > Pengaturan Keuangan > Rekening Bank', breadcrumb_en = 'Company Admin > Finance Settings > Bank Accounts' WHERE code = 'MST-03';

UPDATE permission_groups SET breadcrumb_id = 'Operasional > Manajemen Inventaris > Produk', breadcrumb_en = 'Operations > Inventory Management > Products' WHERE code = 'INV-01';
UPDATE permission_groups SET breadcrumb_id = 'Operasional > Manajemen Inventaris > Kategori Produk', breadcrumb_en = 'Operations > Inventory Management > Product Categories' WHERE code = 'INV-02';
UPDATE permission_groups SET breadcrumb_id = 'Operasional > Manajemen Inventaris > Brand', breadcrumb_en = 'Operations > Inventory Management > Brands' WHERE code = 'INV-03';
UPDATE permission_groups SET breadcrumb_id = 'Operasional > Manajemen Inventaris > Satuan Ukur', breadcrumb_en = 'Operations > Inventory Management > Units of Measure' WHERE code = 'INV-04';

-- Fix incorrect URL paths from V19
UPDATE permission_groups SET url_path = '/master/taxes' WHERE code = 'MST-04';
UPDATE permission_groups SET url_path = '/master/currencies' WHERE code = 'MST-05';
