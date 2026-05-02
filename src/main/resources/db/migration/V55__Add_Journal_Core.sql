CREATE TABLE acc_journal_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(50) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id BIGINT NOT NULL,
    source_code VARCHAR(60),
    posting_date DATE NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_by_user_id BIGINT,
    created_date DATETIME,
    updated_by_user_id BIGINT,
    updated_date DATETIME,
    version INT DEFAULT 1,
    CONSTRAINT uk_acc_journal_source UNIQUE (source_type, source_id)
);

CREATE TABLE acc_journal_lines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    journal_entry_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    account_id BIGINT NOT NULL,
    debit_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    credit_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    created_by_user_id BIGINT,
    created_date DATETIME,
    updated_by_user_id BIGINT,
    updated_date DATETIME,
    version INT DEFAULT 1,
    CONSTRAINT fk_acc_journal_lines_entry FOREIGN KEY (journal_entry_id)
        REFERENCES acc_journal_entries(id) ON DELETE CASCADE
);
