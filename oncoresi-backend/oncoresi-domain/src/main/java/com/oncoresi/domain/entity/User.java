package com.oncoresi.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户领域实体
 */
@Data
public class User {

    private Long id;

    /** 用户名 */
    private String username;

    /** 密码(加密后) */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态: 1-启用 0-禁用 */
    private Integer status;

    /** 角色集合 */
    private Set<Role> roles;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 获取角色代码集合
     */
    public Set<String> getRoleCodes() {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getCode)
                .collect(java.util.stream.Collectors.toSet());
    }
}
