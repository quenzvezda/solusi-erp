-- Development seeder accounting fiscal years and periods.

-- Own only fiscal year / accounting period cleanup for the seeded dev years.

DELETE FROM acc_accounting_periods
WHERE fiscal_year_id IN (
    SELECT id
    FROM acc_fiscal_years
    WHERE code IN ('FY-2024', 'FY-2025')
);

DELETE FROM acc_fiscal_years
WHERE code IN ('FY-2024', 'FY-2025');

INSERT INTO acc_fiscal_years (code, name, start_date, end_date, is_active, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('FY-2024', 'Fiscal Year 2024', '2024-01-01', '2024-12-31', 1, 1, 1, NOW(), 1, NOW()),
('FY-2025', 'Fiscal Year 2025', '2025-01-01', '2025-12-31', 1, 1, 1, NOW(), 1, NOW());

SET @fy2024 = (SELECT id FROM acc_fiscal_years WHERE code = 'FY-2024');
SET @fy2025 = (SELECT id FROM acc_fiscal_years WHERE code = 'FY-2025');

-- FY-2024 monthly periods.
INSERT INTO acc_accounting_periods (code, name, period_number, fiscal_year_id, start_date, end_date, status, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('AP-2024-M01', 'January 2024',   1,  @fy2024, '2024-01-01', '2024-01-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M02', 'February 2024',  2,  @fy2024, '2024-02-01', '2024-02-29', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M03', 'March 2024',     3,  @fy2024, '2024-03-01', '2024-03-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M04', 'April 2024',     4,  @fy2024, '2024-04-01', '2024-04-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M05', 'May 2024',       5,  @fy2024, '2024-05-01', '2024-05-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M06', 'June 2024',      6,  @fy2024, '2024-06-01', '2024-06-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M07', 'July 2024',      7,  @fy2024, '2024-07-01', '2024-07-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M08', 'August 2024',    8,  @fy2024, '2024-08-01', '2024-08-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M09', 'September 2024', 9,  @fy2024, '2024-09-01', '2024-09-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M10', 'October 2024',   10, @fy2024, '2024-10-01', '2024-10-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M11', 'November 2024',  11, @fy2024, '2024-11-01', '2024-11-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2024-M12', 'December 2024',  12, @fy2024, '2024-12-01', '2024-12-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW());

-- FY-2025 monthly periods.
INSERT INTO acc_accounting_periods (code, name, period_number, fiscal_year_id, start_date, end_date, status, version, created_by_user_id, created_date, updated_by_user_id, updated_date)
VALUES
('AP-2025-M01', 'January 2025',   1,  @fy2025, '2025-01-01', '2025-01-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M02', 'February 2025',  2,  @fy2025, '2025-02-01', '2025-02-28', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M03', 'March 2025',     3,  @fy2025, '2025-03-01', '2025-03-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M04', 'April 2025',     4,  @fy2025, '2025-04-01', '2025-04-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M05', 'May 2025',       5,  @fy2025, '2025-05-01', '2025-05-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M06', 'June 2025',      6,  @fy2025, '2025-06-01', '2025-06-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M07', 'July 2025',      7,  @fy2025, '2025-07-01', '2025-07-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M08', 'August 2025',    8,  @fy2025, '2025-08-01', '2025-08-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M09', 'September 2025', 9,  @fy2025, '2025-09-01', '2025-09-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M10', 'October 2025',   10, @fy2025, '2025-10-01', '2025-10-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M11', 'November 2025',  11, @fy2025, '2025-11-01', '2025-11-30', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW()),
('AP-2025-M12', 'December 2025',  12, @fy2025, '2025-12-01', '2025-12-31', 'NEVER_OPENED', 1, 1, NOW(), 1, NOW());
