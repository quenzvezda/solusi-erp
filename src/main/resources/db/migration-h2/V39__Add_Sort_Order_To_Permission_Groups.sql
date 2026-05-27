-- V39: Add sort_order column to permission_groups for deterministic menu ordering.
-- Dashboard gets sort_order=1 so it appears first in the sidebar.

ALTER TABLE permission_groups ADD COLUMN sort_order INT NOT NULL DEFAULT 100;

-- Dashboard first
UPDATE permission_groups SET sort_order = 1 WHERE code = 'DASH-01';

-- Company Admin group (Security, Master Data, Finance Settings) → 10-series
UPDATE permission_groups SET sort_order = 10 WHERE code = 'SEC-01';
UPDATE permission_groups SET sort_order = 11 WHERE code = 'SEC-02';
UPDATE permission_groups SET sort_order = 12 WHERE code = 'SEC-03';
UPDATE permission_groups SET sort_order = 13 WHERE code = 'SEC-04';
UPDATE permission_groups SET sort_order = 20 WHERE code = 'MST-01';
UPDATE permission_groups SET sort_order = 21 WHERE code = 'MST-06';
UPDATE permission_groups SET sort_order = 22 WHERE code = 'MST-02';
UPDATE permission_groups SET sort_order = 30 WHERE code = 'MST-04';
UPDATE permission_groups SET sort_order = 31 WHERE code = 'MST-05';
UPDATE permission_groups SET sort_order = 32 WHERE code = 'MST-03';

-- Operations group (Application, Inventory, etc.) → 50-series
UPDATE permission_groups SET sort_order = 50 WHERE code = 'COM-01';
UPDATE permission_groups SET sort_order = 51 WHERE code = 'COM-02';
UPDATE permission_groups SET sort_order = 60 WHERE code = 'INV-01';
UPDATE permission_groups SET sort_order = 61 WHERE code = 'INV-02';
UPDATE permission_groups SET sort_order = 62 WHERE code = 'INV-03';
UPDATE permission_groups SET sort_order = 63 WHERE code = 'INV-04';
UPDATE permission_groups SET sort_order = 64 WHERE code = 'INV-05';
UPDATE permission_groups SET sort_order = 65 WHERE code = 'INV-06';
UPDATE permission_groups SET sort_order = 66 WHERE code = 'INV-07';
UPDATE permission_groups SET sort_order = 70 WHERE code = 'INV-08';
UPDATE permission_groups SET sort_order = 80 WHERE code = 'INV-09';
UPDATE permission_groups SET sort_order = 81 WHERE code = 'INV-10';
