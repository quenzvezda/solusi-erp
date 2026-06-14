ALTER TABLE inv_movements ADD COLUMN reversal_of_movement_id BIGINT NULL;

ALTER TABLE inv_movements ADD CONSTRAINT fk_inv_mov_reversal_of
    FOREIGN KEY (reversal_of_movement_id) REFERENCES inv_movements(id);

ALTER TABLE inv_movements ADD CONSTRAINT uk_inv_mov_reversal_of UNIQUE (reversal_of_movement_id);

CREATE INDEX idx_inv_mov_reference
    ON inv_movements(reference_type, reference_id);

CREATE INDEX idx_inv_mov_reversal_lookup
    ON inv_movements(reversal_of_movement_id);

ALTER TABLE inv_valuation_layers ADD COLUMN reversal_of_movement_id BIGINT NULL;

ALTER TABLE inv_valuation_layers ADD CONSTRAINT fk_val_layer_reversal_of_movement
    FOREIGN KEY (reversal_of_movement_id) REFERENCES inv_movements(id);

CREATE INDEX idx_val_layer_reversal_movement
    ON inv_valuation_layers(reversal_of_movement_id);
