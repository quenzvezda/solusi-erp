-- V25: Add Serial Number to Stock and Movements
ALTER TABLE inv_stock_balances
    ADD COLUMN serial_number VARCHAR(100) AFTER container_id,
    DROP INDEX uk_inv_stock_prod_cont,
    ADD UNIQUE KEY uk_inv_stock_prod_cont_sn (product_id, container_id, serial_number);

ALTER TABLE inv_movements
    ADD COLUMN serial_number VARCHAR(100) AFTER container_id;
