-- V16__Master_Bank_Account.sql

CREATE TABLE bank_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    bank_name VARCHAR(255) NOT NULL,
    branch VARCHAR(255) NOT NULL,
    city_id BIGINT NOT NULL,
    party_id BIGINT NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_no VARCHAR(100) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    note TEXT,
    
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(50) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(50),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_bank_account_city FOREIGN KEY (city_id) REFERENCES geographics(id),
    CONSTRAINT fk_bank_account_party FOREIGN KEY (party_id) REFERENCES parties(id)
);

-- Seed permissions for BANK-ACCOUNT
INSERT INTO permissions (name, description, created_by, created_date) VALUES
('BANK-ACCOUNT_READ', 'Allow user to read bank account data', 'SYSTEM', NOW()),
('BANK-ACCOUNT_CREATE', 'Allow user to create bank account data', 'SYSTEM', NOW()),
('BANK-ACCOUNT_UPDATE', 'Allow user to update bank account data', 'SYSTEM', NOW()),
('BANK-ACCOUNT_DELETE', 'Allow user to delete bank account data', 'SYSTEM', NOW());

-- Give permissions to ROLE_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' AND p.name LIKE 'BANK-ACCOUNT\_%' ESCAPE '\';

-- Initial Seed for Bank Account Sequence
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by, updated_date)
VALUES ('BANK_ACCOUNT', 'BA-{seq}', 4, 'NEVER', 'SYSTEM', NOW());
