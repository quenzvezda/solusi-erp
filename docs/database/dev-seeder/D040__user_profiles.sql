-- Development seeder user profiles.

INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by_user_id, created_date, version)
SELECT id, 'Budi Santoso', '081234567891', 'id', 10, 'light', 1, NOW(), 1 FROM users WHERE username = 'approver1';

INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by_user_id, created_date, version)
SELECT id, 'Siti Rahayu', '081234567892', 'id', 10, 'light', 1, NOW(), 1 FROM users WHERE username = 'approver2';

INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by_user_id, created_date, version)
SELECT id, 'Ahmad Fadli', '081234567893', 'id', 10, 'light', 1, NOW(), 1 FROM users WHERE username = 'warehouse1';

INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by_user_id, created_date, version)
SELECT id, 'Dewi Lestari', '081234567894', 'id', 10, 'light', 1, NOW(), 1 FROM users WHERE username = 'employee1';
