package com.oncoresi.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.oncoresi.domain.aggregate.UserAggregate;
import com.oncoresi.domain.repository.UserRepository;
import com.oncoresi.domain.valueobject.Password;
import com.oncoresi.types.dto.LoginRequest;
import com.oncoresi.types.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 认证应用服务（Sa-Token）
 * 使用聚合根进行业务操作
 *
 * @author OncoResi Team
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * BCrypt密码编码器适配器
     */
    private final Password.PasswordEncoder domainPasswordEncoder = new Password.PasswordEncoder() {
        @Override
        public boolean matches(String rawPassword, String encryptedPassword) {
            return passwordEncoder.matches(rawPassword, encryptedPassword);
        }

        @Override
        public String encode(String rawPassword) {
            return passwordEncoder.encode(rawPassword);
        }
    };

    /**
     * 用户登录
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户聚合
        UserAggregate userAggregate = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 2. 验证密码（使用值对象）
        Password password = Password.of(userAggregate.getUser().getPassword());
        if (!password.matches(request.getPassword(), domainPasswordEncoder)) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (!userAggregate.getUser().isEnabled()) {
            throw new RuntimeException("用户已被禁用");
        }

        // 4. Sa-Token 登录，记录登录状态
        StpUtil.login(userAggregate.getId());

        // 5. 获取 Token 值
        String token = StpUtil.getTokenValue();

        // 6. 将用户信息和数据权限存储到 Session
        StpUtil.getSession().set("user", userAggregate.getUser());
        StpUtil.getSession().set("userId", userAggregate.getId());
        StpUtil.getSession().set("username", userAggregate.getUser().getUsername());
        StpUtil.getSession().set("roles", userAggregate.getRoleCodes());
        StpUtil.getSession().set("dataScopeType", userAggregate.getDataScopeType());

        // 7. 返回登录信息
        return new LoginResponse(
                token,
                userAggregate.getId(),
                userAggregate.getUser().getUsername(),
                userAggregate.getRoleCodes()
        );
    }

    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        return loginId != null ? Long.parseLong(loginId.toString()) : null;
    }

    /**
     * 获取当前登录用户聚合
     */
    @Transactional(readOnly = true)
    public UserAggregate getCurrentUserAggregate() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 检查当前用户是否拥有指定角色
     */
    public boolean hasRole(String roleCode) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) StpUtil.getSession().get("roles");
        return roles != null && roles.contains(roleCode);
    }
}
