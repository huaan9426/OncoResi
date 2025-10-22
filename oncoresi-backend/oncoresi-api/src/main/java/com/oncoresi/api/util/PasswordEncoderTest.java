package com.oncoresi.api.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类 - 用于生成测试密码
 * 运行main方法生成BCrypt密码
 */
public class PasswordEncoderTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后: " + encodedPassword);
        System.out.println("验证: " + encoder.matches(rawPassword, encodedPassword));
    }
}
