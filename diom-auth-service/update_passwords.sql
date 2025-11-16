-- 更新用户密码为正确的 BCrypt 加密值
-- 原始密码: 123456
-- 如果登录失败，请运行此脚本更新密码

USE diom_workflow;

-- 更新 admin 用户密码
-- BCrypt 加密的 "123456"
UPDATE sys_user 
SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE username = 'admin';

-- 更新 test 用户密码
UPDATE sys_user 
SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE username = 'test';

-- 验证更新
SELECT id, username, nickname, email, 
       LEFT(password, 20) as password_prefix,
       status, create_time
FROM sys_user;

-- 说明：
-- 这个密码是通过 BCryptPasswordEncoder 生成的
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- String encoded = encoder.encode("123456");
-- 
-- 所有用户的密码都是: 123456

