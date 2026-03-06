-- V7: Inventory Module - Product
-- Mandate: AGENTS.md Section 4 & 5

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    barcode VARCHAR(50),
    note TEXT,
    category_id BIGINT NOT NULL,
    uom_id BIGINT NOT NULL, -- Base UoM (e.g. PCS, BOX)
    brand_id BIGINT,
    hscode VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_serialized BOOLEAN NOT NULL DEFAULT FALSE,
    min_stock DECIMAL(19, 4) DEFAULT 0,
    max_stock DECIMAL(19, 4) DEFAULT 0,
    weight_net DECIMAL(19, 4) DEFAULT 0,
    weight_gross DECIMAL(19, 4) DEFAULT 0,
    weight_uom_id BIGINT, -- UoM for Weight (Type: WEIGHT)
    dim_length DECIMAL(19, 4) DEFAULT 0,
    dim_width DECIMAL(19, 4) DEFAULT 0,
    dim_height DECIMAL(19, 4) DEFAULT 0,
    dim_uom_id BIGINT, -- UoM for Dimensions (Type: LENGTH)
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_categories(id),
    CONSTRAINT fk_product_uom FOREIGN KEY (uom_id) REFERENCES unit_of_measures(id),
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brands(id),
    CONSTRAINT fk_product_weight_uom FOREIGN KEY (weight_uom_id) REFERENCES unit_of_measures(id),
    CONSTRAINT fk_product_dim_uom FOREIGN KEY (dim_uom_id) REFERENCES unit_of_measures(id)
) ENGINE=InnoDB;

-- Initial Seed for Product Sequence
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by, updated_date)
VALUES ('PRODUCT', 'PRD-{seq}', 4, 'NEVER', 'SYSTEM', NOW());

-- Seed Permissions for Product
INSERT INTO permissions (name, description, created_by, created_date) VALUES 
('PRODUCT_READ', 'Melihat Daftar Produk', 'SYSTEM', NOW()),
('PRODUCT_CREATE', 'Menambah Produk Baru', 'SYSTEM', NOW()),
('PRODUCT_UPDATE', 'Mengubah Data Produk', 'SYSTEM', NOW()),
('PRODUCT_DELETE', 'Menghapus Produk', 'SYSTEM', NOW());

-- Add New Permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'ROLE_ADMIN' 
AND p.name LIKE 'PRODUCT\_%' ESCAPE '\\';
