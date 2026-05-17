-- V9000: E2E Seed Data
-- Runs after all schema migrations. Sets up data needed for E2E tests.
-- Convention: V9000+ reserved for E2E-only seed data.

-- Ensure admin user can login without password change prompt
-- (SystemInitializer sets password_change_required=true, we override for E2E)
UPDATE users SET password_change_required = false WHERE username = 'admin';

-- ====== E2E MASTER DATA ======
-- ID range 9001+ reserved for E2E test data

-- Unit of Measures
INSERT INTO unit_of_measures (id, code, name, type, created_by_user_id, created_date, version)
VALUES
  (9001, 'E2E-PCS', 'E2E Piece', 'UNIT', 1, NOW(), 1),
  (9002, 'E2E-KG', 'E2E Kilogram', 'WEIGHT', 1, NOW(), 1),
  (9003, 'E2E-CM', 'E2E Centimeter', 'LENGTH', 1, NOW(), 1);

-- Product Categories
INSERT INTO product_categories (id, code, name, type, note, created_by_user_id, created_date, version)
VALUES
  (9001, 'E2E-CAT-STOCK', 'E2E Category Stock', 'STOCK', 'E2E test category for stock items', 1, NOW(), 1),
  (9002, 'E2E-CAT-SVC', 'E2E Category Service', 'SERVICE', 'E2E test category for services', 1, NOW(), 1);

-- Brands
INSERT INTO brands (id, code, name, created_by_user_id, created_date, version)
VALUES
  (9001, 'E2E-BRAND-A', 'E2E Brand Alpha', 1, NOW(), 1),
  (9002, 'E2E-BRAND-B', 'E2E Brand Beta', 1, NOW(), 1);

