-- V27: FIFO Valuation Layers and Inventory Movement Costing
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

ALTER TABLE inv_movements
    ADD COLUMN unit_cost_currency_id BIGINT AFTER reference_code,
    ADD COLUMN unit_cost_exchange_rate DECIMAL(19,6) AFTER unit_cost_currency_id,
    ADD COLUMN unit_cost_amount_original DECIMAL(19,4) AFTER unit_cost_exchange_rate,
    ADD COLUMN unit_cost_amount_local DECIMAL(19,4) AFTER unit_cost_amount_original,
    ADD CONSTRAINT fk_inv_mov_currency FOREIGN KEY (unit_cost_currency_id) REFERENCES master_currencies(id);
