-- Development seeder users linked to seeded parties.

-- BCrypt hash for "admin123"
SET @pwd = '$2b$10$kJlPG9rZovRB57u0POwoQujVA0EK4kI6qWpUZJD/O8fa5WxfyAGca';

SET @role_approver_id = (SELECT id FROM roles WHERE name = 'ROLE_APPROVER');
SET @role_warehouse_id = (SELECT id FROM roles WHERE name = 'ROLE_WAREHOUSE');
SET @role_employee_id = (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE');
SET @p_apr1 = (SELECT id FROM parties WHERE code = 'BP-DEV-APR01');
SET @p_apr2 = (SELECT id FROM parties WHERE code = 'BP-DEV-APR02');
SET @p_wh1 = (SELECT id FROM parties WHERE code = 'BP-DEV-WH01');
SET @p_emp1 = (SELECT id FROM parties WHERE code = 'BP-DEV-EMP01');

INSERT INTO users (username, password, email, enabled, password_change_required, role_id, party_id, created_by_user_id, created_date, version) VALUES
('approver1', @pwd, 'budi.santoso@solusierp.com', 1, 0, @role_approver_id, @p_apr1, 1, NOW(), 1),
('approver2', @pwd, 'siti.rahayu@solusierp.com',  1, 0, @role_approver_id, @p_apr2, 1, NOW(), 1),
('warehouse1', @pwd, 'ahmad.fadli@solusierp.com', 1, 0, @role_warehouse_id, @p_wh1, 1, NOW(), 1),
('employee1', @pwd, 'dewi.lestari@solusierp.com', 1, 0, @role_employee_id, @p_emp1, 1, NOW(), 1);
