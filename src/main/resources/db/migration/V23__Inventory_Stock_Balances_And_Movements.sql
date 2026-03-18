-- V23: Inventory Stock Balances and Movements

-- 1. Stock Balances Table
CREATE TABLE inv_stock_balances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    container_id BIGINT NOT NULL,
    quantity DECIMAL(19,4) NOT NULL DEFAULT 0,
    reserved_quantity DECIMAL(19,4) NOT NULL DEFAULT 0,
    
    -- Audit Columns
    version BIGINT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_inv_stock_prod_cont (product_id, container_id),
    CONSTRAINT fk_inv_stock_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inv_stock_container FOREIGN KEY (container_id) REFERENCES inv_containers(id),
    CONSTRAINT fk_inv_stock_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_inv_stock_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Inventory Movements Table
CREATE TABLE inv_movements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_date DATETIME(6) NOT NULL,
    product_id BIGINT NOT NULL,
    container_id BIGINT NOT NULL,
    quantity DECIMAL(19,4) NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    reference_code VARCHAR(100),
    
    -- Audit Columns
    version BIGINT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    CONSTRAINT fk_inv_mov_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inv_mov_container FOREIGN KEY (container_id) REFERENCES inv_containers(id),
    CONSTRAINT fk_inv_mov_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_inv_mov_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
