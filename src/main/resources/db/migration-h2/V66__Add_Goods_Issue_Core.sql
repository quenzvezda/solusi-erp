-- V66: Generic Goods Issue Core

CREATE TABLE IF NOT EXISTS inv_goods_issues (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    code               VARCHAR(30)   NOT NULL,
    issue_date         DATE          NOT NULL,
    reference_type     VARCHAR(40)   NOT NULL,
    reference_id       BIGINT        NULL,
    reference_code     VARCHAR(60)   NULL,
    party_id           BIGINT        NULL,
    party_type         VARCHAR(40)   NULL,
    facility_id        BIGINT        NOT NULL,
    currency_id        BIGINT        NULL,
    exchange_rate      DECIMAL(19,6) NULL,
    status             VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    note               TEXT          NULL,
    version            INT           NOT NULL DEFAULT 0,
    created_by_user_id BIGINT        NULL,
    created_date       DATETIME      NULL,
    updated_by_user_id BIGINT        NULL,
    updated_date       DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_gi_code (code),
    CONSTRAINT fk_gi_party         FOREIGN KEY (party_id)            REFERENCES parties(id),
    CONSTRAINT fk_gi_facility      FOREIGN KEY (facility_id)         REFERENCES inv_facilities(id),
    CONSTRAINT fk_gi_currency      FOREIGN KEY (currency_id)         REFERENCES master_currencies(id),
    CONSTRAINT fk_gi_created_by    FOREIGN KEY (created_by_user_id)  REFERENCES users(id),
    CONSTRAINT fk_gi_updated_by    FOREIGN KEY (updated_by_user_id)  REFERENCES users(id)
);

CREATE INDEX idx_gi_status ON inv_goods_issues(status);
CREATE INDEX idx_gi_issue_date ON inv_goods_issues(issue_date);
CREATE INDEX idx_gi_reference_type_id ON inv_goods_issues(reference_type, reference_id);
CREATE INDEX idx_gi_party_type_id ON inv_goods_issues(party_type, party_id);
CREATE INDEX idx_gi_facility ON inv_goods_issues(facility_id);

CREATE TABLE IF NOT EXISTS inv_goods_issue_lines (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    header_id             BIGINT        NOT NULL,
    reference_line_id     BIGINT        NULL,
    product_id            BIGINT        NOT NULL,
    is_serialized         BOOLEAN       NOT NULL DEFAULT FALSE,
    quantity_issued       DECIMAL(19,4) NOT NULL,
    uom_id                BIGINT        NOT NULL,
    base_quantity         DECIMAL(19,4) NOT NULL,
    facility_id           BIGINT        NOT NULL,
    grid_id               BIGINT        NOT NULL,
    container_id          BIGINT        NOT NULL,
    serial_number         VARCHAR(255)  NULL,
    unit_cost             DECIMAL(19,6) NULL,
    inventory_amount      DECIMAL(19,4) NULL,
    tax_base_amount       DECIMAL(19,4) NULL,
    tax_amount            DECIMAL(19,4) NULL,
    clearing_amount       DECIMAL(19,4) NULL,
    valuation_ref_type    VARCHAR(50)   NULL,
    valuation_ref_id      BIGINT        NULL,
    valuation_ref_line_id BIGINT        NULL,
    version               INT           NOT NULL DEFAULT 0,
    created_by_user_id    BIGINT        NULL,
    created_date          DATETIME      NULL,
    updated_by_user_id    BIGINT        NULL,
    updated_date          DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_gil_header      FOREIGN KEY (header_id)           REFERENCES inv_goods_issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_gil_product     FOREIGN KEY (product_id)          REFERENCES products(id),
    CONSTRAINT fk_gil_uom         FOREIGN KEY (uom_id)              REFERENCES unit_of_measures(id),
    CONSTRAINT fk_gil_facility    FOREIGN KEY (facility_id)         REFERENCES inv_facilities(id),
    CONSTRAINT fk_gil_grid        FOREIGN KEY (grid_id)             REFERENCES inv_grids(id),
    CONSTRAINT fk_gil_container   FOREIGN KEY (container_id)        REFERENCES inv_containers(id),
    CONSTRAINT fk_gil_created_by  FOREIGN KEY (created_by_user_id)  REFERENCES users(id),
    CONSTRAINT fk_gil_updated_by  FOREIGN KEY (updated_by_user_id)  REFERENCES users(id)
);

CREATE INDEX idx_gil_reference_line ON inv_goods_issue_lines(reference_line_id);
CREATE INDEX idx_gil_valuation_ref ON inv_goods_issue_lines(valuation_ref_type, valuation_ref_id, valuation_ref_line_id);

INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('GOODS_ISSUE', 'GI-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, sort_order, created_by_user_id, created_date)
VALUES
('INV-13', 'Pengeluaran Barang', 'Goods Issue',
 'Operasional > Transaksi Inventaris > Pengeluaran Barang', 'Operations > Inventory Transactions > Goods Issue',
 '/inventory/goods-issues', 'ti-package-export',
 'Kelola dokumen pengeluaran barang generik', 'Manage generic goods issue documents',
 230, 1, NOW());

INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('GOODS-ISSUE_READ',     'Melihat daftar pengeluaran barang',             1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-13')),
('GOODS-ISSUE_CREATE',   'Membuat pengeluaran barang baru',               1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-13')),
('GOODS-ISSUE_UPDATE',   'Mengubah pengeluaran barang',                   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-13')),
('GOODS-ISSUE_DELETE',   'Menghapus pengeluaran barang draft',            1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-13')),
('GOODS-ISSUE_COMPLETE', 'Menyelesaikan pengeluaran barang',              1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-13')),
('GOODS-ISSUE_CANCEL',   'Membatalkan pengeluaran barang yang selesai',   1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-13'));

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('GOODS-ISSUE_READ', 'GOODS-ISSUE_CREATE', 'GOODS-ISSUE_UPDATE', 'GOODS-ISSUE_DELETE', 'GOODS-ISSUE_COMPLETE', 'GOODS-ISSUE_CANCEL');

