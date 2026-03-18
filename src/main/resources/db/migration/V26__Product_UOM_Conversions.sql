-- V26: Product UOM Conversions
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
