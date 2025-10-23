package com.oncoresi.infra.security;

import com.oncoresi.infra.persistence.mapper.DataScopeMapper;
import com.oncoresi.infra.persistence.mapper.UserMapper;
import com.oncoresi.infra.persistence.po.DataScopePO;
import com.oncoresi.infra.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 数据权限服务
 * 负责加载和缓存用户的数据权限信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataScopeService {

    private final DataScopeMapper dataScopeMapper;
    private final UserMapper userMapper;

    /**
     * 加载用户数据权限上下文
     */
    public DataScopeContext loadUserDataScope(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 查询用户信息
            UserPO user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                return null;
            }

            // 查询数据权限配置
            DataScopePO dataScope = dataScopeMapper.selectByUserId(userId);
            if (dataScope == null) {
                // 默认为个人权限
                log.warn("用户数据权限未配置，使用默认SELF权限: userId={}", userId);
                return DataScopeContext.self(userId);
            }

            // 构建数据权限上下文
            return switch (dataScope.getScopeType()) {
                case "ALL" -> DataScopeContext.all(userId, user.getHospitalId());
                case "DEPT" -> DataScopeContext.dept(userId, dataScope.getDeptId(), user.getHospitalId());
                case "SUPERVISED" -> DataScopeContext.supervised(userId, user.getHospitalId());
                case "SELF" -> DataScopeContext.self(userId);
                default -> {
                    log.warn("未知的数据权限类型: {}", dataScope.getScopeType());
                    yield DataScopeContext.self(userId);
                }
            };

        } catch (Exception e) {
            log.error("加载用户数据权限失败: userId={}", userId, e);
            return DataScopeContext.self(userId);
        }
    }

    /**
     * 刷新当前用户的数据权限上下文
     */
    public void refreshCurrentUserDataScope() {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId != null) {
            DataScopeContext context = loadUserDataScope(userId);
            SecurityContextHolder.setDataScopeContext(context);
        }
    }
}
