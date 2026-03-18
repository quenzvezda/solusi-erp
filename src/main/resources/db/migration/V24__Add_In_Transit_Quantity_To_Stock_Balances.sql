-- V24: Add In-Transit Quantity to Stock Balances
ALTER TABLE inv_stock_balances
    ADD COLUMN in_transit_quantity DECIMAL(19,4) NOT NULL DEFAULT 0 AFTER reserved_quantity;
