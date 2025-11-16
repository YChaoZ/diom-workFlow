package com.diom.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成器测试
 * 用于生成 BCrypt 加密的密码
 */
public class PasswordGeneratorTest {

    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        
        String separator = "============================================================";
        System.out.println(separator);
        System.out.println("原始密码: " + password);
        System.out.println(separator);
        System.out.println();
        
        // 生成多个哈希值用于测试
        for (int i = 1; i <= 5; i++) {
            String hash = encoder.encode(password);
            boolean matches = encoder.matches(password, hash);
            
            System.out.println("密码哈希 " + i + ":");
            System.out.println(hash);
            System.out.println("验证结果: " + (matches ? "✓ 正确" : "✗ 错误"));
            System.out.println("长度: " + hash.length());
            System.out.println();
        }
        
        System.out.println(separator);
        System.out.println("请复制上面任意一个密码哈希值到数据库中");
        System.out.println(separator);
    }
    
    @Test
    public void verifyExistingPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String existingHash = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";
        
        System.out.println("验证现有密码哈希:");
        System.out.println("密码: " + password);
        System.out.println("哈希: " + existingHash);
        System.out.println("结果: " + (encoder.matches(password, existingHash) ? "✓ 正确" : "✗ 错误"));
    }
}

