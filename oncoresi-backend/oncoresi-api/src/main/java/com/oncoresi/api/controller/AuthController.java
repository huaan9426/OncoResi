package com.oncoresi.api.controller;

import com.oncoresi.application.service.AuthService;
import com.oncoresi.types.dto.LoginRequest;
import com.oncoresi.types.dto.LoginResponse;
import com.oncoresi.types.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试接口 - 需要认证
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("认证成功");
    }
}
