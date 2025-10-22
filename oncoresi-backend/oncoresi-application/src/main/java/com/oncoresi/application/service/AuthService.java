package com.oncoresi.application.service;

import com.oncoresi.domain.entity.User;
import com.oncoresi.domain.repository.UserRepository;
import com.oncoresi.types.dto.LoginRequest;
import com.oncoresi.types.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 用户登录
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (!user.isEnabled()) {
            throw new RuntimeException("用户已被禁用");
        }

        // 4. 生成JWT token
        String token = jwtService.generateToken(user);

        // 5. 返回登录信息
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRoleCodes()
        );
    }
}
