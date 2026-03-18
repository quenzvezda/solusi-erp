-- V29: Stock Adjustment Schema
-- Standard: AGENTS.md Section 4 & 5

-- 1. Stock Adjustment Header Table
CREATE TABLE inv_stock_adjustments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    transaction_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    note TEXT,
    
    -- CurrencyAmount (Embeddable)
    currency_id BIGINT,
    total_exchange_rate DECIMAL(19,6),
    total_amount_original DECIMAL(19,4),
    total_amount_local DECIMAL(19,4),
    
    -- Audit Columns
    version INT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_adj_currency FOREIGN KEY (currency_id) REFERENCES master_currencies(id),
    CONSTRAINT fk_stock_adj_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_stock_adj_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Stock Adjustment Line Table
CREATE TABLE inv_stock_adjustment_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    header_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    container_id BIGINT NOT NULL,
    quantity DECIMAL(19,4) NOT NULL,
    unit_cost DECIMAL(19,4) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    serial_number VARCHAR(100),
    
    -- Audit Columns
    version INT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_adj_line_header FOREIGN KEY (header_id) REFERENCES inv_stock_adjustments(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_adj_line_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_stock_adj_line_container FOREIGN KEY (container_id) REFERENCES inv_containers(id),
    CONSTRAINT fk_stock_adj_line_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_stock_adj_line_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Register Sequence for Stock Adjustment
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date) VALUES
('STOCK_ADJUSTMENT', 'ADJ-{yy}{mm}-{seq}', 5, 'MONTHLY', 1, NOW());
