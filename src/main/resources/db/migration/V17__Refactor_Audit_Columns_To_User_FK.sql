-- V17: Refactor Audit Columns to FK-based User References
-- Changes: rename created_by/updated_by (VARCHAR) to created_by_user_id/updated_by_user_id (BIGINT FK)
-- Backfill: all existing rows set to user ID 1 (admin)
-- Exceptions: users & user_profiles columns stay nullable with NO FK constraint (chicken-and-egg)

-- ============================================================
-- SECURITY MODULE
-- ============================================================

-- permissions
ALTER TABLE permissions
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE permissions SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE permissions
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE permissions
    ADD CONSTRAINT fk_permissions_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_permissions_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE permissions
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- roles
ALTER TABLE roles
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE roles SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE roles
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE roles
    ADD CONSTRAINT fk_roles_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_roles_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE roles
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- users (EXCEPTION: nullable, no FK constraint — chicken-and-egg)
ALTER TABLE users
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE users SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE users
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- user_profiles (EXCEPTION: nullable, no FK constraint — chicken-and-egg)
ALTER TABLE user_profiles
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE user_profiles SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE user_profiles
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- ============================================================
-- INVENTORY MODULE
-- ============================================================

-- product_categories
ALTER TABLE product_categories
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE product_categories SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE product_categories
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE product_categories
    ADD CONSTRAINT fk_product_categories_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_product_categories_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE product_categories
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- unit_of_measures
ALTER TABLE unit_of_measures
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE unit_of_measures SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE unit_of_measures
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE unit_of_measures
    ADD CONSTRAINT fk_unit_of_measures_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_unit_of_measures_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE unit_of_measures
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- brands
ALTER TABLE brands
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE brands SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE brands
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE brands
    ADD CONSTRAINT fk_brands_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_brands_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE brands
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- products
ALTER TABLE products
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE products SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE products
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE products
    ADD CONSTRAINT fk_products_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_products_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE products
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- ============================================================
-- MASTER MODULE
-- ============================================================

-- taxes
ALTER TABLE taxes
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE taxes SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE taxes
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE taxes
    ADD CONSTRAINT fk_taxes_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_taxes_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE taxes
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- master_currencies
ALTER TABLE master_currencies
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE master_currencies SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE master_currencies
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE master_currencies
    ADD CONSTRAINT fk_master_currencies_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_master_currencies_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE master_currencies
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- geographics
ALTER TABLE geographics
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE geographics SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE geographics
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE geographics
    ADD CONSTRAINT fk_geographics_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_geographics_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE geographics
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- bank_accounts
ALTER TABLE bank_accounts
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE bank_accounts SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE bank_accounts
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE bank_accounts
    ADD CONSTRAINT fk_bank_accounts_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_bank_accounts_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE bank_accounts
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- parties
ALTER TABLE parties
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE parties SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE parties
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE parties
    ADD CONSTRAINT fk_parties_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_parties_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE parties
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- party_addresses
ALTER TABLE party_addresses
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE party_addresses SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE party_addresses
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE party_addresses
    ADD CONSTRAINT fk_party_addresses_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_party_addresses_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE party_addresses
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- party_identifications
ALTER TABLE party_identifications
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE party_identifications SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE party_identifications
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE party_identifications
    ADD CONSTRAINT fk_party_identifications_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_party_identifications_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE party_identifications
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- party_contacts
ALTER TABLE party_contacts
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE party_contacts SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE party_contacts
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE party_contacts
    ADD CONSTRAINT fk_party_contacts_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_party_contacts_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE party_contacts
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- party_role_types
ALTER TABLE party_role_types
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE party_role_types SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE party_role_types
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE party_role_types
    ADD CONSTRAINT fk_party_role_types_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_party_role_types_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE party_role_types
    DROP COLUMN created_by,
    DROP COLUMN updated_by;

-- party_id_types
ALTER TABLE party_id_types
    ADD COLUMN created_by_user_id BIGINT NULL,
    ADD COLUMN updated_by_user_id BIGINT NULL;
UPDATE party_id_types SET created_by_user_id = 1, updated_by_user_id = 1;
ALTER TABLE party_id_types
    MODIFY created_by_user_id BIGINT NOT NULL;
ALTER TABLE party_id_types
    ADD CONSTRAINT fk_party_id_types_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_party_id_types_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);
ALTER TABLE party_id_types
    DROP COLUMN created_by,
    DROP COLUMN updated_by;
