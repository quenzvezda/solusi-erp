-- V9: Party Module Redesign
-- Changes: ContactMechanism, Soft-Delete on Address/Identification,
--          Salutation on Party, PartyRoleType CRUD permissions

-- ============================================================
-- 1. Add salutation column to parties
-- ============================================================
ALTER TABLE parties
    ADD COLUMN salutation VARCHAR(50) NULL AFTER code;

-- ============================================================
-- 2. Soft-delete on party_addresses (isActive + isDefault)
-- ============================================================
ALTER TABLE party_addresses
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER country,
    ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE AFTER is_active;

-- ============================================================
-- 3. Soft-delete on party_identifications (isActive + isDefault)
-- ============================================================
ALTER TABLE party_identifications
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER expiry_date,
    ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE AFTER is_active;

-- ============================================================
-- 4. New table: party_contacts (ContactMechanism)
-- ============================================================
CREATE TABLE party_contacts (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    party_id   BIGINT       NOT NULL,
    label      VARCHAR(100) NOT NULL COMMENT 'e.g. PIC Sales, Direktur',
    mobile     VARCHAR(50)  NULL,
    phone      VARCHAR(50)  NULL,
    email      VARCHAR(100) NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by    VARCHAR(100) NOT NULL,
    created_date  DATETIME     NOT NULL,
    updated_by    VARCHAR(100) NULL,
    updated_date  DATETIME     NULL,
    version       INT          NOT NULL DEFAULT 1,
    CONSTRAINT fk_party_contact_party FOREIGN KEY (party_id)
        REFERENCES parties(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 5. PartyRoleType CRUD – Permissions + Sequence
-- ============================================================
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by, updated_date)
VALUES ('PARTY-ROLE-TYPE', 'PRT-{seq}', 5, 'NEVER', 'SYSTEM', NOW());

INSERT INTO permissions (name, description, created_by, created_date) VALUES
('PARTY-ROLE-TYPE_READ',   'Melihat Daftar Tipe Peran Partner',  'SYSTEM', NOW()),
('PARTY-ROLE-TYPE_CREATE', 'Menambah Tipe Peran Partner Baru',   'SYSTEM', NOW()),
('PARTY-ROLE-TYPE_UPDATE', 'Mengubah Data Tipe Peran Partner',   'SYSTEM', NOW()),
('PARTY-ROLE-TYPE_DELETE', 'Menghapus Tipe Peran Partner',       'SYSTEM', NOW());

-- Grant to ROLE_ADMIN (use ESCAPE to avoid _ wildcard issue)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name LIKE 'PARTY-ROLE-TYPE\_%' ESCAPE '\\';
