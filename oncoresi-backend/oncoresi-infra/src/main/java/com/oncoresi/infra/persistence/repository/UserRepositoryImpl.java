package com.oncoresi.infra.persistence.repository;

import com.oncoresi.application.event.DomainEventPublisher;
import com.oncoresi.domain.aggregate.UserAggregate;
import com.oncoresi.domain.entity.Role;
import com.oncoresi.domain.entity.User;
import com.oncoresi.domain.repository.UserRepository;
import com.oncoresi.infra.persistence.converter.UserConverter;
import com.oncoresi.infra.persistence.mapper.DataScopeMapper;
import com.oncoresi.infra.persistence.mapper.RoleMapper;
import com.oncoresi.infra.persistence.mapper.UserMapper;
import com.oncoresi.infra.persistence.po.DataScopePO;
import com.oncoresi.infra.persistence.po.UserPO;
import com.oncoresi.infra.security.DataScopeContext.DataScopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户聚合仓储实现（MyBatis-Flex）
 * 负责加载和保存整个用户聚合（用户+角色+数据权限）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final DataScopeMapper dataScopeMapper;
    private final UserConverter userConverter;
    private final DomainEventPublisher eventPublisher;

    @Override
    public Optional<UserAggregate> findByUsername(String username) {
        UserPO userPO = userMapper.selectByUsername(username);
        if (userPO == null) {
            return Optional.empty();
        }

        return Optional.of(loadAggregate(userPO));
    }

    @Override
    public Optional<UserAggregate> findById(Long id) {
        UserPO userPO = userMapper.selectOneById(id);
        if (userPO == null) {
            return Optional.empty();
        }

        return Optional.of(loadAggregate(userPO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAggregate save(UserAggregate aggregate) {
        // 1. 保存用户基本信息
        UserPO userPO = userConverter.toPO(aggregate.getUser());

        if (userPO.getId() == null) {
            // 新增用户
            userMapper.insert(userPO);
            log.info("新增用户: id={}, username={}", userPO.getId(), userPO.getUsername());
        } else {
            // 更新用户
            userMapper.update(userPO);
            log.debug("更新用户: id={}", userPO.getId());
        }

        Long userId = userPO.getId();

        // 2. 保存角色关系（先删除旧关系，再插入新关系）
        userMapper.deleteUserRoles(userId);
        Set<String> roleCodes = aggregate.getRoleCodes();
        for (String roleCode : roleCodes) {
            Long roleId = roleMapper.selectByCode(roleCode).getId();
            userMapper.insertUserRole(userId, roleId);
        }
        log.debug("保存用户角色: userId={}, roles={}", userId, roleCodes);

        // 3. 保存数据权限
        DataScopePO existingScope = dataScopeMapper.selectByUserId(userId);
        DataScopePO dataScopePO = new DataScopePO();
        dataScopePO.setUserId(userId);
        dataScopePO.setScopeType(aggregate.getDataScopeType().name());
        dataScopePO.setDeptId(aggregate.getDeptId());

        if (existingScope == null) {
            dataScopeMapper.insert(dataScopePO);
        } else {
            dataScopePO.setId(existingScope.getId());
            dataScopeMapper.update(dataScopePO);
        }
        log.debug("保存数据权限: userId={}, scopeType={}", userId, aggregate.getDataScopeType());

        // 4. 发布领域事件
        if (aggregate.hasDomainEvents()) {
            eventPublisher.publishAll(aggregate.getDomainEvents());
            aggregate.clearDomainEvents();
        }

        // 5. 返回保存后的聚合
        return findById(userId).orElseThrow(
                () -> new IllegalStateException("保存用户聚合失败")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        // 级联删除由数据库外键约束处理
        userMapper.deleteById(userId);
        log.info("删除用户聚合: userId={}", userId);
    }

    /**
     * 加载完整的用户聚合
     */
    private UserAggregate loadAggregate(UserPO userPO) {
        Long userId = userPO.getId();

        // 1. 转换用户实体
        User user = userConverter.toDomain(userPO);

        // 2. 加载角色
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(userId);
        Set<Role> roles = roleCodes.stream()
                .map(code -> {
                    var rolePO = roleMapper.selectByCode(code);
                    return new Role(rolePO.getId(), rolePO.getCode(), rolePO.getName(), rolePO.getDescription());
                })
                .collect(Collectors.toSet());

        // 3. 加载数据权限
        DataScopePO dataScopePO = dataScopeMapper.selectByUserId(userId);
        DataScopeType dataScopeType = dataScopePO != null ?
                DataScopeType.valueOf(dataScopePO.getScopeType()) :
                DataScopeType.SELF; // 默认个人权限

        Long deptId = dataScopePO != null ? dataScopePO.getDeptId() : null;

        // 4. 重建聚合
        return UserAggregate.reconstitute(
                user,
                roles,
                dataScopeType,
                deptId,
                userPO.getHospitalId()
        );
    }
}
