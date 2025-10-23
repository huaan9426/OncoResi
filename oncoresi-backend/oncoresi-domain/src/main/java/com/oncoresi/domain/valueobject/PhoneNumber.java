package com.oncoresi.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 手机号值对象（不可变）
 */
public record PhoneNumber(String value) {

    private static final Pattern CHINA_MOBILE_PATTERN = Pattern.compile(
            "^1[3-9]\\d{9}$"
    );

    public PhoneNumber {
        Objects.requireNonNull(value, "手机号不能为空");
        String cleaned = value.replaceAll("[\\s-]", ""); // 去除空格和横线
        if (!CHINA_MOBILE_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("无效的手机号格式: " + value);
        }
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    /**
     * 格式化手机号（138-0013-8000）
     */
    public String formatted() {
        String cleaned = value.replaceAll("[\\s-]", "");
        return cleaned.substring(0, 3) + "-" +
               cleaned.substring(3, 7) + "-" +
               cleaned.substring(7);
    }

    /**
     * 脱敏显示（138****8000）
     */
    public String masked() {
        String cleaned = value.replaceAll("[\\s-]", "");
        return cleaned.substring(0, 3) + "****" + cleaned.substring(7);
    }
}
