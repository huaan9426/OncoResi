package com.oncoresi.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.oncoresi.application.service.AuthService;
import com.oncoresi.domain.entity.User;
import com.oncoresi.types.dto.LoginRequest;
import com.oncoresi.types.dto.LoginResponse;
import com.oncoresi.types.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器（Sa-Token）
 *
 * @author OncoResi Team
 */
@Tag(name = "认证管理", description = "用户登录、登出、权限验证等接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "退出登录，清除 Token")
    @PostMapping("/logout")
    @SaCheckLogin
    public Result<Void> logout() {
        authService.logout();
        return Result.success(null, "登出成功");
    }

    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current")
    @SaCheckLogin
    public Result<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        if (user == null) {
            return Result.error("未登录或登录已过期");
        }
        // 移除敏感信息
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 检查登录状态
     */
    @Operation(summary = "检查登录状态", description = "检查当前 Token 是否有效")
    @GetMapping("/check")
    public Result<Map<String, Object>> checkLogin() {
        Map<String, Object> data = new HashMap<>();
        data.put("isLogin", StpUtil.isLogin());
        if (StpUtil.isLogin()) {
            data.put("userId", StpUtil.getLoginId());
            data.put("tokenValue", StpUtil.getTokenValue());
        }
        return Result.success(data);
    }

    /**
     * 测试接口 - 需要登录
     */
    @Operation(summary = "测试接口", description = "测试 Sa-Token 认证是否正常工作")
    @GetMapping("/test")
    @SaCheckLogin
    public Result<String> test() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success("认证成功！当前用户ID: " + userId);
    }

    /**
     * 测试接口 - 需要管理员角色
     */
    @Operation(summary = "管理员测试接口", description = "测试角色权限控制")
    @GetMapping("/admin-test")
    @SaCheckRole("HOSPITAL_ADMIN")
    public Result<String> adminTest() {
        return Result.success("恭喜！您拥有医院管理员权限");
    }
}

