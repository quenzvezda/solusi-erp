-- V75: Confirmed Purchase Return Reversal
-- H2 mirror for Purchase Return reversal audit and permission seed.

ALTER TABLE pur_purchase_returns
    ADD COLUMN IF NOT EXISTS reversal_date DATE NULL;

ALTER TABLE pur_purchase_returns
    ADD COLUMN IF NOT EXISTS reversal_reason VARCHAR(500) NULL;

ALTER TABLE pur_purchase_returns
    ADD COLUMN IF NOT EXISTS reversed_by_user_id BIGINT NULL;

ALTER TABLE pur_purchase_returns
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT NULL;

ALTER TABLE pur_purchase_returns
    ADD CONSTRAINT fk_purchase_return_reversed_by
        FOREIGN KEY (reversed_by_user_id) REFERENCES users(id);

ALTER TABLE pur_purchase_returns
    ADD CONSTRAINT fk_purchase_return_reversal_journal
        FOREIGN KEY (reversal_journal_entry_id) REFERENCES acc_journal_entries(id);

CREATE INDEX idx_purchase_return_reversal_date
    ON pur_purchase_returns(reversal_date);

CREATE INDEX idx_purchase_return_reversal_journal
    ON pur_purchase_returns(reversal_journal_entry_id);

CREATE TABLE IF NOT EXISTS pur_purchase_return_reversal_lines (
    id                       BIGINT        NOT NULL AUTO_INCREMENT,
    purchase_return_id       BIGINT        NOT NULL,
    purchase_return_line_id  BIGINT        NULL,
    original_movement_id     BIGINT        NOT NULL,
    target_container_id      BIGINT        NOT NULL,
    product_id               BIGINT        NOT NULL,
    serial_number            VARCHAR(100)  NULL,
    quantity                 DECIMAL(19,4) NOT NULL,
    version                  INT           NOT NULL DEFAULT 0,
    created_by_user_id       BIGINT        NULL,
    created_date             DATETIME      NULL,
    updated_by_user_id       BIGINT        NULL,
    updated_date             DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_pr_reversal_line_movement UNIQUE (original_movement_id),
    CONSTRAINT fk_pr_reversal_line_header
        FOREIGN KEY (purchase_return_id) REFERENCES pur_purchase_returns(id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_reversal_line_purchase_return_line
        FOREIGN KEY (purchase_return_line_id) REFERENCES pur_purchase_return_lines(id),
    CONSTRAINT fk_pr_reversal_line_movement
        FOREIGN KEY (original_movement_id) REFERENCES inv_movements(id),
    CONSTRAINT fk_pr_reversal_line_target_container
        FOREIGN KEY (target_container_id) REFERENCES inv_containers(id),
    CONSTRAINT fk_pr_reversal_line_product
        FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_pr_reversal_line_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_pr_reversal_line_updated_by
        FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_pr_reversal_line_header
    ON pur_purchase_return_reversal_lines(purchase_return_id);

CREATE INDEX idx_pr_reversal_line_pr_line
    ON pur_purchase_return_reversal_lines(purchase_return_line_id);

CREATE INDEX idx_pr_reversal_line_target_container
    ON pur_purchase_return_reversal_lines(target_container_id);

INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id)
VALUES (
    'PURCHASE-RETURN_REVERSE',
    'Membalik retur pembelian yang sudah dikonfirmasi',
    1,
    NOW(),
    (SELECT id FROM permission_groups WHERE code = 'PUR-04')
)
ON DUPLICATE KEY UPDATE name = name;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name = 'PURCHASE-RETURN_REVERSE';
