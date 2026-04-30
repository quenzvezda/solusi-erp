-- Development seeder cleanup for core non-accounting data.

-- Inventory demo data
DELETE FROM products WHERE code LIKE 'PRD-DEMO-%';
DELETE FROM brands WHERE code LIKE 'BRD-DEMO-%';
DELETE FROM product_categories WHERE code LIKE 'CAT-DEMO-%';
DELETE FROM inv_containers WHERE code LIKE 'BIN-DEMO-%';
DELETE FROM inv_grids WHERE code LIKE 'GRD-DEMO-%';
DELETE FROM inv_facilities WHERE code LIKE 'FAC-DEMO-%';

-- Approval data (clean slate for testing)
DELETE FROM appr_signatures;
DELETE FROM appr_histories;
DELETE FROM appr_requests;

-- News data
DELETE FROM common_news WHERE 1=1;

-- Reset admin password_change_required so login works immediately after seeding
UPDATE users SET password_change_required = 0 WHERE username = 'admin';

-- User-party links for dev users (including admin to allow party cleanup)
UPDATE users SET party_id = NULL WHERE username IN ('admin','approver1','approver2','warehouse1','employee1');

-- User profiles for dev users
DELETE FROM user_profiles WHERE user_id IN (SELECT id FROM users WHERE username IN ('approver1','approver2','warehouse1','employee1'));

-- Dev users
DELETE FROM users WHERE username IN ('approver1','approver2','warehouse1','employee1');

-- Party cleanup (dev codes)
DELETE FROM party_address_types WHERE party_address_id IN (SELECT id FROM party_addresses WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01'));
DELETE FROM party_contacts WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01');
DELETE FROM party_identifications WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01');
DELETE FROM party_addresses WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01');
DELETE FROM party_roles WHERE party_id IN (SELECT id FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01');
DELETE FROM parties WHERE code LIKE 'BP-DEV-%' OR code = 'PRT-INTERNAL-01';

-- Role permissions for dev roles
DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM roles WHERE name IN ('ROLE_APPROVER','ROLE_WAREHOUSE','ROLE_EMPLOYEE'));
DELETE FROM roles WHERE name IN ('ROLE_APPROVER','ROLE_WAREHOUSE','ROLE_EMPLOYEE');

-- Remove legacy ROLE_STAFF if still exists
DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM roles WHERE name = 'ROLE_STAFF');
DELETE FROM roles WHERE name = 'ROLE_STAFF';
