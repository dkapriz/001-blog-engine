INSERT INTO captcha_codes(id, code, secret_code, time)
VALUES (10, 'xajefihiz', '4PFn62dKkbdUqGIeqOBMbr', NOW()),
(11, 'nohuhocina', 'vfbPFX9eVuUIVdJRCwchow', NOW());

INSERT INTO users(id, email, password, is_moderator, name, reg_time)
VALUES (10, 'test@mail.ru', '$2a$12$KpEbwzDcKFIEFhxeNTioC.fTyXgtKsZu23GqWZkEiJJRMGJRJBD5i', 1, 'Test', NOW());