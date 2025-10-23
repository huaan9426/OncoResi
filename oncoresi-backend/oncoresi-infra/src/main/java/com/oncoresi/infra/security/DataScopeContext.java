package com.oncoresi.infra.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据权限上下文
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeContext {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 数据权限类型: ALL, DEPT, SUPERVISED, SELF
     */
    private DataScopeType scopeType;

    /**
     * 科室ID（DEPT类型时使用）
     */
    private Long deptId;

    /**
     * 医院ID
     */
    private Long hospitalId;

    /**
     * 数据权限类型枚举
     */
    public enum DataScopeType {
        /**
         * 全院数据权限（医院管理员）
         */
        ALL,

        /**
         * 科室数据权限（科室管理员、教师）
         */
        DEPT,

        /**
         * 带教学员数据权限（责任导师）
         */
        SUPERVISED,

        /**
         * 个人数据权限（学员等）
         */
        SELF
    }

    /**
     * 创建全院权限
     */
    public static DataScopeContext all(Long userId, Long hospitalId) {
        return new DataScopeContext(userId, DataScopeType.ALL, null, hospitalId);
    }

    /**
     * 创建科室权限
     */
    public static DataScopeContext dept(Long userId, Long deptId, Long hospitalId) {
        return new DataScopeContext(userId, DataScopeType.DEPT, deptId, hospitalId);
    }

    /**
     * 创建导师权限
     */
    public static DataScopeContext supervised(Long userId, Long hospitalId) {
        return new DataScopeContext(userId, DataScopeType.SUPERVISED, null, hospitalId);
    }

    /**
     * 创建个人权限
     */
    public static DataScopeContext self(Long userId) {
        return new DataScopeContext(userId, DataScopeType.SELF, null, null);
    }
}
