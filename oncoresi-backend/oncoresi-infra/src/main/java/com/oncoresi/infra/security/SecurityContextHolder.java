package com.oncoresi.infra.security;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 安全上下文持有者
 * 用于获取当前登录用户的信息
 */
public class SecurityContextHolder {

    private static final ThreadLocal<DataScopeContext> DATA_SCOPE_CONTEXT = new ThreadLocal<>();

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置数据权限上下文
     */
    public static void setDataScopeContext(DataScopeContext context) {
        DATA_SCOPE_CONTEXT.set(context);
    }

    /**
     * 获取数据权限上下文
     */
    public static DataScopeContext getDataScopeContext() {
        return DATA_SCOPE_CONTEXT.get();
    }

    /**
     * 清除数据权限上下文
     */
    public static void clearDataScopeContext() {
        DATA_SCOPE_CONTEXT.remove();
    }

    /**
     * 判断是否已登录
     */
    public static boolean isAuthenticated() {
        try {
            return StpUtil.isLogin();
        } catch (Exception e) {
            return false;
        }
    }
}
