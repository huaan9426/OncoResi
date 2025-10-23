package com.oncoresi.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 邮箱值对象（不可变）
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public Email {
        Objects.requireNonNull(value, "邮箱不能为空");
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("无效的邮箱格式: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    /**
     * 获取邮箱域名
     */
    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }

    /**
     * 获取用户名部分
     */
    public String getLocalPart() {
        return value.substring(0, value.indexOf('@'));
    }
}
