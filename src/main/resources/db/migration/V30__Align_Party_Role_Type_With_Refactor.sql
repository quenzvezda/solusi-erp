-- Ensure legacy DBs have columns required by refactored PartyRoleType entity.
ALTER TABLE party_role_types
    ADD COLUMN IF NOT EXISTS note TEXT NULL AFTER name,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER note;
