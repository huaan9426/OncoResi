package com.oncoresi.domain.valueobject;

import java.util.Objects;

/**
 * 密码值对象（不可变）
 * 封装密码验证逻辑
 */
public record Password(String encryptedValue) {

    public Password {
        Objects.requireNonNull(encryptedValue, "密码不能为空");
        if (encryptedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空字符串");
        }
    }

    /**
     * 创建密码值对象（用于已加密的密码）
     */
    public static Password of(String encrypted) {
        return new Password(encrypted);
    }

    /**
     * 验证密码是否匹配（需要传入密码编码器）
     * 注意：这里使用接口抽象，避免领域层依赖具体的加密实现
     */
    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.encryptedValue);
    }

    /**
     * 密码编码器接口（领域层定义，基础设施层实现）
     */
    public interface PasswordEncoder {
        boolean matches(String rawPassword, String encryptedPassword);
        String encode(String rawPassword);
    }
}
