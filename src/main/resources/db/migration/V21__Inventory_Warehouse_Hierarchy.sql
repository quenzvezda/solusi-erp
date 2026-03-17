-- V21: Inventory Warehouse Hierarchy (Facility, Grid, Container)

-- 1. Facilities Table
CREATE TABLE inv_facilities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    owner_id BIGINT NOT NULL,
    address_line1 TEXT,
    city_id BIGINT,
    postal_code VARCHAR(20),
    note TEXT,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    
    -- Audit Columns
    version BIGINT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_inv_facilities_code (code),
    CONSTRAINT fk_inv_fac_owner FOREIGN KEY (owner_id) REFERENCES parties(id),
    CONSTRAINT fk_inv_fac_city FOREIGN KEY (city_id) REFERENCES geographics(id),
    CONSTRAINT fk_inv_fac_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_inv_fac_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Grids Table
CREATE TABLE inv_grids (
    id BIGINT NOT NULL AUTO_INCREMENT,
    facility_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    note TEXT,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    
    -- Audit Columns
    version BIGINT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_inv_grids_fac_code (facility_id, code),
    CONSTRAINT fk_inv_grids_facility FOREIGN KEY (facility_id) REFERENCES inv_facilities(id),
    CONSTRAINT fk_inv_grids_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_inv_grids_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Containers Table
CREATE TABLE inv_containers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    grid_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    barcode VARCHAR(100),
    length DECIMAL(10,2),
    width DECIMAL(10,2),
    height DECIMAL(10,2),
    max_weight DECIMAL(10,2),
    note TEXT,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    
    -- Audit Columns
    version BIGINT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT,
    created_date DATETIME(6),
    updated_by_user_id BIGINT,
    updated_date DATETIME(6),
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_inv_cont_grid_code (grid_id, code),
    UNIQUE KEY uk_inv_cont_barcode (barcode),
    CONSTRAINT fk_inv_cont_grid FOREIGN KEY (grid_id) REFERENCES inv_grids(id),
    CONSTRAINT fk_inv_cont_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_inv_cont_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Initial Sequences
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle) VALUES 
('FACILITY', 'FAC-{seq}', 4, 'NEVER'),
('CONTAINER', 'BIN-{seq}', 5, 'NEVER');
