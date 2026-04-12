-- Add po_type column to purchase orders table
ALTER TABLE pur_purchase_orders
    ADD COLUMN po_type VARCHAR(10) NOT NULL DEFAULT 'DIRECT' AFTER pr_id;
