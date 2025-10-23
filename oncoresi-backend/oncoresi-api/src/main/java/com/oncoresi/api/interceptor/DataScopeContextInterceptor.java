package com.oncoresi.api.interceptor;

import com.oncoresi.infra.security.DataScopeContext;
import com.oncoresi.infra.security.DataScopeService;
import com.oncoresi.infra.security.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 数据权限上下文拦截器
 * 在每次请求时自动加载当前用户的数据权限上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeContextInterceptor implements HandlerInterceptor {

    private final DataScopeService dataScopeService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 如果用户已登录，加载数据权限上下文
            if (SecurityContextHolder.isAuthenticated()) {
                Long userId = SecurityContextHolder.getCurrentUserId();
                DataScopeContext context = dataScopeService.loadUserDataScope(userId);

                if (context != null) {
                    SecurityContextHolder.setDataScopeContext(context);
                    log.debug("数据权限上下文已加载: userId={}, scopeType={}",
                            userId, context.getScopeType());
                }
            }
        } catch (Exception e) {
            log.error("加载数据权限上下文失败", e);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 清除ThreadLocal，防止内存泄漏
        SecurityContextHolder.clearDataScopeContext();
    }
}
