package com.oncoresi.domain.repository;

import com.oncoresi.domain.entity.User;

import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository {

    /**
     * 根据用户名查询用户(包含角色信息)
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据ID查询用户
     */
    Optional<User> findById(Long id);

    /**
     * 保存用户
     */
    User save(User user);
}
