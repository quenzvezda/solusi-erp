-- V18: Update System Sequences Audit Columns
-- Aligns system_sequences with the new FK-based auditing (AuditorAware<Long>)

ALTER TABLE system_sequences
    ADD COLUMN updated_by_user_id BIGINT NULL;

UPDATE system_sequences SET updated_by_user_id = 1;

ALTER TABLE system_sequences ALTER COLUMN updated_by_user_id SET NOT NULL;

-- Note: We don't necessarily need a FK constraint here if we want to avoid circular dependencies during initial setup,
-- but for consistency with other tables:
ALTER TABLE system_sequences
    ADD CONSTRAINT fk_system_sequences_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id);

ALTER TABLE system_sequences
    DROP COLUMN updated_by;
