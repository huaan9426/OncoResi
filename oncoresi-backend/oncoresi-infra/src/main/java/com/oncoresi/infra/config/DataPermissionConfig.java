package com.oncoresi.infra.config;

import com.oncoresi.infra.security.DataScopeInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 数据权限配置
 */
@Configuration
@RequiredArgsConstructor
public class DataPermissionConfig {

    private final SqlSessionFactory sqlSessionFactory;

    /**
     * 注册数据权限拦截器
     */
    @PostConstruct
    public void addDataScopeInterceptor() {
        sqlSessionFactory.getConfiguration().addInterceptor(new DataScopeInterceptor());
    }
}
