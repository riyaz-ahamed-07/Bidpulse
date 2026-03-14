-- create a test user with simple hashed password placeholder (not production)
INSERT INTO app_users (email, name, password_hash)
VALUES ('test@local', 'Test User', 'password-hash-placeholder');

-- create wallet for user id 1 (adjust ID if different)
INSERT INTO wallet (user_id, balance, reserved_amount)
VALUES (1, 1000.00, 0.00)
ON CONFLICT (user_id) DO NOTHING;