-- V13: Refactor Party Address to use Geographic and multi-purpose types
-- Mandate: AGENTS.md Section 4 & 5

-- 1. Create table for multi-purpose address types
CREATE TABLE party_address_types (
    party_address_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    PRIMARY KEY (party_address_id, type),
    CONSTRAINT fk_pat_party_address FOREIGN KEY (party_address_id) REFERENCES party_addresses(id) ON DELETE CASCADE
);

-- 2. Refactor party_addresses table
-- Drop old columns
ALTER TABLE party_addresses DROP COLUMN type;
ALTER TABLE party_addresses DROP COLUMN city;
ALTER TABLE party_addresses DROP COLUMN province;
ALTER TABLE party_addresses DROP COLUMN country;

-- Add new city_id FK column
ALTER TABLE party_addresses ADD COLUMN city_id BIGINT;
ALTER TABLE party_addresses ADD CONSTRAINT fk_party_address_city FOREIGN KEY (city_id) REFERENCES geographics(id);
