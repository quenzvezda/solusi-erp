-- Development seeder parties and related master data.

INSERT INTO party_role_types (code, name, note, is_active, created_by_user_id, created_date, version)
SELECT 'APPROVER', 'Approver', 'Party yang berwenang menyetujui dokumen', 1, 1, NOW(), 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM party_role_types WHERE code = 'APPROVER');

-- Party role type IDs
SET @prt_internal = (SELECT id FROM party_role_types WHERE code = 'INTERNAL');
SET @prt_customer = (SELECT id FROM party_role_types WHERE code = 'CUSTOMER');
SET @prt_supplier = (SELECT id FROM party_role_types WHERE code = 'SUPPLIER');
SET @prt_employee = (SELECT id FROM party_role_types WHERE code = 'EMPLOYEE');
SET @prt_warehouse = (SELECT id FROM party_role_types WHERE code = 'WAREHOUSE_OPERATOR');
SET @prt_approver = (SELECT id FROM party_role_types WHERE code = 'APPROVER');

-- ID type IDs
SET @idt_ktp = (SELECT id FROM party_id_types WHERE code = 'KTP');
SET @idt_npwp = (SELECT id FROM party_id_types WHERE code = 'NPWP');
SET @idt_nib = (SELECT id FROM party_id_types WHERE code = 'NIB');
SET @idt_passport = (SELECT id FROM party_id_types WHERE code = 'PASSPORT');

-- Geographic IDs
SET @geo_jkt_pusat = 213; -- Kota Adm. Jakarta Pusat
SET @geo_jkt_selatan = 216; -- Kota Adm. Jakarta Selatan
SET @geo_jkt_timur = 217; -- Kota Adm. Jakarta Timur

-- Party: Solusi Program (Organization — Internal Owner)
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('PRT-INTERNAL-01', 'PT.', 'Solusi Program', 'ORGANIZATION', 1, 'info@solusierp.com', '021-5551000', 1, NOW(), 1, NOW(), 1);
SET @p_owner = (SELECT id FROM parties WHERE code = 'PRT-INTERNAL-01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_owner, @prt_internal);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_owner, 'Jl. Sudirman No. 1, Gedung Solusi Lt. 5', @geo_jkt_pusat, '10220', 1, 1, 1, NOW(), 1);
SET @addr_owner = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_owner, 'OFFICE');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_owner, 'Kantor Pusat', NULL, '021-5551000', 'info@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_owner, @idt_npwp, '01.234.567.8-012.000', '2020-01-15', 1, 1, 1, NOW(), 1);
INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_owner, @idt_nib, '9120012345678', '2020-03-01', 1, 0, 1, NOW(), 1);

-- Party: Administrator (Person — linked to admin user)
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-ADMIN', 'Bpk.', 'Administrator Utama', 'PERSON', 1, 'admin@solusierp.com', '081234567890', 1, NOW(), 1, NOW(), 1);
SET @p_admin = (SELECT id FROM parties WHERE code = 'BP-DEV-ADMIN');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_admin, @prt_internal), (@p_admin, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_admin, 'Jl. Sudirman No. 1, Gedung Solusi Lt. 5', @geo_jkt_pusat, '10220', 1, 1, 1, NOW(), 1);
SET @addr_admin = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_admin, 'OFFICE');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_admin, 'Personal', '081234567890', NULL, 'admin@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_admin, @idt_ktp, '3171012345670001', '2020-06-15', 1, 1, 1, NOW(), 1);

-- Link admin user to party
UPDATE users SET party_id = @p_admin WHERE username = 'admin';

-- Party: Budi Santoso (Approver 1)
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-APR01', 'Bpk.', 'Budi Santoso', 'PERSON', 1, 'budi.santoso@solusierp.com', '081234567891', 1, NOW(), 1, NOW(), 1);
SET @p_apr1 = (SELECT id FROM parties WHERE code = 'BP-DEV-APR01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_apr1, @prt_approver), (@p_apr1, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr1, 'Jl. Gatot Subroto No. 45, Pancoran', @geo_jkt_selatan, '12780', 1, 1, 1, NOW(), 1);
SET @addr_apr1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_apr1, 'HOME');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr1, 'Personal', '081234567891', NULL, 'budi.santoso@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr1, @idt_ktp, '3171012345670002', '2021-03-10', 1, 1, 1, NOW(), 1);

-- Party: Siti Rahayu (Approver 2)
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-APR02', 'Ibu', 'Siti Rahayu', 'PERSON', 1, 'siti.rahayu@solusierp.com', '081234567892', 1, NOW(), 1, NOW(), 1);
SET @p_apr2 = (SELECT id FROM parties WHERE code = 'BP-DEV-APR02');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_apr2, @prt_approver), (@p_apr2, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr2, 'Jl. Casablanca No. 12, Tebet', @geo_jkt_selatan, '12870', 1, 1, 1, NOW(), 1);
SET @addr_apr2 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_apr2, 'HOME');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr2, 'Personal', '081234567892', NULL, 'siti.rahayu@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_apr2, @idt_ktp, '3171012345670003', '2021-07-22', 1, 1, 1, NOW(), 1);

-- Party: PT. Maju Jaya (Customer 1)
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, notes, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-CUST01', 'PT.', 'Maju Jaya', 'ORGANIZATION', 1, 'purchasing@majujaya.co.id', '021-5552001', 'Customer utama sektor retail', 1, NOW(), 1, NOW(), 1);
SET @p_cust1 = (SELECT id FROM parties WHERE code = 'BP-DEV-CUST01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_cust1, @prt_customer);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust1, 'Jl. Mangga Dua Raya No. 88, Pademangan', @geo_jkt_pusat, '10730', 1, 1, 1, NOW(), 1);
SET @addr_cust1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_cust1, 'OFFICE'), (@addr_cust1, 'BILLING');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust1, 'PIC Purchasing', '081299887766', '021-5552001', 'purchasing@majujaya.co.id', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust1, @idt_npwp, '02.345.678.9-013.000', '2019-05-20', 1, 1, 1, NOW(), 1);
INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust1, @idt_nib, '9120098765432', '2019-06-01', 1, 0, 1, NOW(), 1);

-- Party: CV. Berkah Sejahtera (Customer 2)
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, notes, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-CUST02', 'CV.', 'Berkah Sejahtera', 'ORGANIZATION', 1, 'order@berkahsejahtera.id', '021-5553002', 'Customer UMKM sektor F&B', 1, NOW(), 1, NOW(), 1);
SET @p_cust2 = (SELECT id FROM parties WHERE code = 'BP-DEV-CUST02');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_cust2, @prt_customer);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust2, 'Jl. Kalibata Raya No. 5, Pancoran', @geo_jkt_selatan, '12740', 1, 1, 1, NOW(), 1);
SET @addr_cust2 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_cust2, 'OFFICE');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust2, 'Owner', '081388776655', NULL, 'order@berkahsejahtera.id', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_cust2, @idt_npwp, '03.456.789.0-014.000', '2022-01-10', 1, 1, 1, NOW(), 1);

-- Party: PT. Sumber Makmur (Supplier)
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, notes, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-SUP01', 'PT.', 'Sumber Makmur', 'ORGANIZATION', 1, 'sales@sumbermakmur.co.id', '021-5554003', 'Supplier utama elektronik dan gadget', 1, NOW(), 1, NOW(), 1);
SET @p_sup1 = (SELECT id FROM parties WHERE code = 'BP-DEV-SUP01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_sup1, @prt_supplier);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, 'Kawasan Industri Pulogadung Blok A No. 10', @geo_jkt_timur, '13920', 1, 1, 1, NOW(), 1);
SET @addr_sup1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_sup1, 'FACTORY'), (@addr_sup1, 'SHIPPING');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, 'PIC Sales', '081277665544', '021-5554003', 'sales@sumbermakmur.co.id', 1, 1, 1, NOW(), 1);
INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, 'Gudang', NULL, '021-5554004', 'gudang@sumbermakmur.co.id', 1, 0, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, @idt_npwp, '04.567.890.1-015.000', '2018-08-15', 1, 1, 1, NOW(), 1);
INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_sup1, @idt_nib, '9120011223344', '2018-09-01', 1, 0, 1, NOW(), 1);

-- Party: Ahmad Fadli (Warehouse Operator)
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-WH01', 'Bpk.', 'Ahmad Fadli', 'PERSON', 1, 'ahmad.fadli@solusierp.com', '081234567893', 1, NOW(), 1, NOW(), 1);
SET @p_wh1 = (SELECT id FROM parties WHERE code = 'BP-DEV-WH01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_wh1, @prt_warehouse), (@p_wh1, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_wh1, 'Jl. Cempaka Putih Tengah No. 20', @geo_jkt_pusat, '10510', 1, 1, 1, NOW(), 1);
SET @addr_wh1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_wh1, 'HOME');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_wh1, 'Personal', '081234567893', NULL, 'ahmad.fadli@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_wh1, @idt_ktp, '3171012345670004', '2022-01-05', 1, 1, 1, NOW(), 1);

-- Party: Dewi Lestari (Employee)
INSERT INTO parties (code, salutation, name, type, is_active, email, phone, created_by_user_id, created_date, updated_by_user_id, updated_date, version)
VALUES ('BP-DEV-EMP01', 'Ibu', 'Dewi Lestari', 'PERSON', 1, 'dewi.lestari@solusierp.com', '081234567894', 1, NOW(), 1, NOW(), 1);
SET @p_emp1 = (SELECT id FROM parties WHERE code = 'BP-DEV-EMP01');

INSERT INTO party_roles (party_id, role_type_id) VALUES (@p_emp1, @prt_employee);

INSERT INTO party_addresses (party_id, address_line1, city_id, postal_code, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_emp1, 'Jl. Kebagusan Raya No. 15, Pasar Minggu', @geo_jkt_selatan, '12520', 1, 1, 1, NOW(), 1);
SET @addr_emp1 = LAST_INSERT_ID();
INSERT INTO party_address_types (party_address_id, type) VALUES (@addr_emp1, 'HOME');

INSERT INTO party_contacts (party_id, label, mobile, phone, email, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_emp1, 'Personal', '081234567894', NULL, 'dewi.lestari@solusierp.com', 1, 1, 1, NOW(), 1);

INSERT INTO party_identifications (party_id, id_type_id, id_number, issued_date, is_active, is_default, created_by_user_id, created_date, version)
VALUES (@p_emp1, @idt_ktp, '3171012345670005', '2023-02-14', 1, 1, 1, NOW(), 1);
