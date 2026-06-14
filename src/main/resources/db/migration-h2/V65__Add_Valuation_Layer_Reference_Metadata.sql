ALTER TABLE inv_valuation_layers ADD COLUMN reference_type VARCHAR(50) NULL;
ALTER TABLE inv_valuation_layers ADD COLUMN reference_id BIGINT NULL;
ALTER TABLE inv_valuation_layers ADD COLUMN reference_line_id BIGINT NULL;

CREATE INDEX idx_val_layer_reference
    ON inv_valuation_layers(reference_type, reference_id, reference_line_id);
