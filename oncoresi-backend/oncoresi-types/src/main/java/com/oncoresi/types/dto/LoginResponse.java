package com.oncoresi.types.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * 登录响应DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /** JWT token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 角色代码列表 */
    private Set<String> roles;
}
