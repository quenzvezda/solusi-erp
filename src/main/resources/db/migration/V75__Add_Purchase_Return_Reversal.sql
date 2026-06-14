-- V75: Confirmed Purchase Return Reversal
-- Adds explicit reversal audit data for Purchase Return documents that were already confirmed.

-- 1. Purchase Return header reversal audit metadata
ALTER TABLE pur_purchase_returns
    ADD COLUMN IF NOT EXISTS reversal_date DATE NULL AFTER generated_gi_id,
    ADD COLUMN IF NOT EXISTS reversal_reason VARCHAR(500) NULL AFTER reversal_date,
    ADD COLUMN IF NOT EXISTS reversed_by_user_id BIGINT NULL AFTER reversal_reason,
    ADD COLUMN IF NOT EXISTS reversal_journal_entry_id BIGINT NULL AFTER reversed_by_user_id;

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

-- 2. Immutable target-location snapshots for each original outbound movement
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
    UNIQUE KEY uk_pr_reversal_line_movement (original_movement_id),
    KEY idx_pr_reversal_line_header (purchase_return_id),
    KEY idx_pr_reversal_line_pr_line (purchase_return_line_id),
    KEY idx_pr_reversal_line_target_container (target_container_id),
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Permission boundary: reverse confirmed Purchase Return, distinct from pre-confirm cancel
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
