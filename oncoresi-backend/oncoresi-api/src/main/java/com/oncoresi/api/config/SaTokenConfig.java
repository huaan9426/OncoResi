package com.oncoresi.api.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.oncoresi.api.interceptor.DataScopeContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 *
 * @author OncoResi Team
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final DataScopeContextInterceptor dataScopeContextInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 注册数据权限上下文拦截器（优先级最高，在登录校验之前）
        registry.addInterceptor(dataScopeContextInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // 2. 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定路由需要登录认证（排除登录、注册等公开接口）
            SaRouter.match("/**")
                    .notMatch(
                            "/auth/login",           // 登录接口
                            "/auth/register",        // 注册接口（如果有）
                            "/doc.html",             // Swagger UI
                            "/swagger-ui/**",        // Swagger UI 资源
                            "/v3/api-docs/**",       // OpenAPI 文档
                            "/favicon.ico",          // 图标
                            "/actuator/**",          // 监控端点
                            "/error"                 // 错误页面
                    )
                    .check(r -> StpUtil.checkLogin());  // 登录校验
        }))
        .addPathPatterns("/**")
        .order(2);
    }
}
