-- V23: Inventory Core, Valuation, and Movements (Squashed V23-V27)

-- 1. Stock Balances Table
CREATE TABLE inv_stock_balances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    container_id BIGINT NOT NULL,
    serial_number VARCHAR(100),
    quantity DECIMAL(19,4) NOT NULL DEFAULT 0,
    reserved_quantity DECIMAL(19,4) NOT NULL DEFAULT 0,
    in_transit_quantity DECIMAL(19,4) NOT NULL DEFAULT 0,
    
    -- Audit Columns
    version BIGINT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_inv_stock_prod_cont_sn (product_id, container_id, serial_number),
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
    serial_number VARCHAR(100),
    quantity DECIMAL(19,4) NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    reference_code VARCHAR(100),
    
    -- unit_cost (CurrencyAmount)
    unit_cost_currency_id BIGINT,
    unit_cost_exchange_rate DECIMAL(19,6),
    unit_cost_amount_original DECIMAL(19,4),
    unit_cost_amount_local DECIMAL(19,4),
    
    -- Audit Columns
    version BIGINT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    CONSTRAINT fk_inv_mov_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inv_mov_container FOREIGN KEY (container_id) REFERENCES inv_containers(id),
    CONSTRAINT fk_inv_mov_currency FOREIGN KEY (unit_cost_currency_id) REFERENCES master_currencies(id),
    CONSTRAINT fk_inv_mov_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_inv_mov_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Product UOM Conversions
CREATE TABLE product_uom_conversions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    from_uom_id BIGINT NOT NULL,
    to_uom_id BIGINT NOT NULL,
    conversion_factor DECIMAL(19,6) NOT NULL,
    
    -- Audit Columns
    version INT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_prod_uom_conv (product_id, from_uom_id, to_uom_id),
    CONSTRAINT fk_uom_conv_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_uom_conv_from_uom FOREIGN KEY (from_uom_id) REFERENCES unit_of_measures(id),
    CONSTRAINT fk_uom_conv_to_uom FOREIGN KEY (to_uom_id) REFERENCES unit_of_measures(id),
    CONSTRAINT fk_uom_conv_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_uom_conv_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. FIFO Valuation Layers
CREATE TABLE inv_valuation_layers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    container_id BIGINT NOT NULL,
    serial_number VARCHAR(100),
    initial_quantity DECIMAL(19,4) NOT NULL,
    remaining_quantity DECIMAL(19,4) NOT NULL,
    
    -- unit_cost (CurrencyAmount)
    unit_cost_currency_id BIGINT,
    unit_cost_exchange_rate DECIMAL(19,6),
    unit_cost_amount_original DECIMAL(19,4),
    unit_cost_amount_local DECIMAL(19,4),
    
    -- Audit Columns
    version INT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    CONSTRAINT fk_val_layer_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_val_layer_container FOREIGN KEY (container_id) REFERENCES inv_containers(id),
    CONSTRAINT fk_val_layer_currency FOREIGN KEY (unit_cost_currency_id) REFERENCES master_currencies(id),
    CONSTRAINT fk_val_layer_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_val_layer_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
