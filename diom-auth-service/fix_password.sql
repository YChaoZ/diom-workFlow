USE diom_workflow;

-- 更新admin用户密码为 123456
-- BCrypt加密的 "123456"
UPDATE sys_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOs' WHERE username = 'admin';

-- 更新test用户密码为 123456
UPDATE sys_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOs' WHERE username = 'test';

SELECT username, LEFT(password, 60) as password FROM sys_user WHERE username IN ('admin', 'test');

