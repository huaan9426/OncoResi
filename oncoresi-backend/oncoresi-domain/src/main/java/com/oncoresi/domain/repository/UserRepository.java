package com.oncoresi.domain.repository;

import com.oncoresi.domain.aggregate.UserAggregate;

import java.util.Optional;

/**
 * 用户聚合仓储接口
 * 只对聚合根开放，负责加载和保存整个聚合
 */
public interface UserRepository {

    /**
     * 根据用户名查询用户聚合（包含角色、数据权限等完整信息）
     */
    Optional<UserAggregate> findByUsername(String username);

    /**
     * 根据ID查询用户聚合
     */
    Optional<UserAggregate> findById(Long id);

    /**
     * 保存用户聚合（保存聚合根及其内部实体）
     */
    UserAggregate save(UserAggregate aggregate);

    /**
     * 删除用户聚合
     */
    void delete(Long userId);
}
