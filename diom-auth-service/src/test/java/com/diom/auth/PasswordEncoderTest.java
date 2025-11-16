package com.diom.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码编码测试
 * 用于生成 BCrypt 加密的密码
 *
 * @author diom
 */
public class PasswordEncoderTest {

    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成密码
        String password = "123456";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println();
        
        // 验证密码
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("密码验证结果: " + matches);
    }

    @Test
    public void generateMultiplePasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String[] passwords = {"123456", "admin123", "test123"};
        
        System.out.println("=== 批量生成密码 ===");
        for (String password : passwords) {
            String encoded = encoder.encode(password);
            System.out.println("原始密码: " + password);
            System.out.println("加密密码: " + encoded);
            System.out.println("---");
        }
    }
}

