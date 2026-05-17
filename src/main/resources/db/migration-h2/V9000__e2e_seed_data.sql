-- V9000: E2E Seed Data
-- Runs after all schema migrations. Sets up data needed for E2E tests.
-- Convention: V9000+ reserved for E2E-only seed data.

-- Ensure admin user can login without password change prompt
-- (SystemInitializer sets password_change_required=true, we override for E2E)
UPDATE users SET password_change_required = false WHERE username = 'admin';
