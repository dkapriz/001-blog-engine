INSERT INTO captcha_codes(id, code, secret_code, time) VALUES
(10, 'xajefihiz', '4PFn62dKkbdUqGIeqOBMbr', NOW()),
(11, 'nohuhocina', 'vfbPFX9eVuUIVdJRCwchow', NOW()),
(12, 'testCaptcha', 'vfbPFX9eVuUIVdJR', NOW());

INSERT INTO users(id, code, email, password, is_moderator, name, reg_time) VALUES
(10, '594f7a47-038a-49d8-8868-3baa7c7be4e2', 'test@mail.ru',
'$2a$12$KpEbwzDcKFIEFhxeNTioC.fTyXgtKsZu23GqWZkEiJJRMGJRJBD5i', 1, 'Test', NOW());