package com.oncoresi.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 通用配置类
 *
 * @author OncoResi Team
 */
@Configuration
public class CommonConfig {

    /**
     * 密码加密器（BCrypt）
     * <p>
     * 注意：虽然移除了 Spring Security，但仍需要 BCrypt 来加密和验证密码
     * spring-security-crypto 是一个独立的库，不依赖 Spring Security 核心
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
