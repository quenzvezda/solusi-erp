ALTER TABLE users ADD COLUMN party_id BIGINT UNIQUE AFTER role_id;
ALTER TABLE users ADD CONSTRAINT fk_users_party FOREIGN KEY (party_id) REFERENCES parties(id);
