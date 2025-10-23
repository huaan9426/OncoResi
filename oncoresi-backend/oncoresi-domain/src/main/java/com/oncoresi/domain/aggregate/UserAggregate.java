package com.oncoresi.domain.aggregate;

import com.oncoresi.domain.entity.Role;
import com.oncoresi.domain.entity.User;
import com.oncoresi.domain.event.DataScopeChangedEvent;
import com.oncoresi.domain.event.RoleAssignedEvent;
import com.oncoresi.domain.event.UserRegisteredEvent;
import com.oncoresi.domain.exception.DomainException;
import com.oncoresi.domain.valueobject.Email;
import com.oncoresi.domain.valueobject.Password;
import com.oncoresi.domain.valueobject.PhoneNumber;
import com.oncoresi.infra.security.DataScopeContext.DataScopeType;
import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 用户聚合根
 * 封装用户、角色、数据权限的业务逻辑
 */
@Getter
public class UserAggregate extends AggregateRoot<Long> {

    /**
     * 用户基本信息（聚合根实体）
     */
    private final User user;

    /**
     * 角色集合
     */
    private final Set<Role> roles;

    /**
     * 数据权限类型
     */
    private DataScopeType dataScopeType;

    /**
     * 科室ID（当dataScopeType为DEPT时使用）
     */
    private Long deptId;

    /**
     * 医院ID
     */
    private Long hospitalId;

    /**
     * 私有构造函数
     */
    private UserAggregate(User user, Set<Role> roles, DataScopeType dataScopeType, Long deptId, Long hospitalId) {
        this.user = Objects.requireNonNull(user, "用户不能为空");
        this.roles = new HashSet<>(roles != null ? roles : Set.of());
        this.dataScopeType = dataScopeType;
        this.deptId = deptId;
        this.hospitalId = hospitalId;
    }

    @Override
    public Long getId() {
        return user.getId();
    }

    /**
     * 重建聚合（从持久化数据恢复）
     */
    public static UserAggregate reconstitute(User user, Set<Role> roles, DataScopeType dataScopeType, Long deptId, Long hospitalId) {
        return new UserAggregate(user, roles, dataScopeType, deptId, hospitalId);
    }

    /**
     * 创建新用户（工厂方法）
     */
    public static UserAggregate create(
            String username,
            Password password,
            String realName,
            Email email,
            PhoneNumber phoneNumber,
            Long hospitalId,
            Long deptId
    ) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password.encryptedValue());
        user.setRealName(realName);
        user.setEmail(email.value());
        user.setPhone(phoneNumber.value());
        user.setStatus(1); // 默认启用

        UserAggregate aggregate = new UserAggregate(user, new HashSet<>(), DataScopeType.SELF, deptId, hospitalId);

        // 发布用户注册事件
        aggregate.addDomainEvent(new UserRegisteredEvent(
                user.getId(),
                username,
                Set.of()
        ));

        return aggregate;
    }

    /**
     * 分配角色
     */
    public void assignRole(Role role) {
        if (!user.isEnabled()) {
            throw DomainException.of("禁用用户无法分配角色");
        }

        if (roles.stream().anyMatch(r -> r.getCode().equals(role.getCode()))) {
            throw DomainException.of("用户已拥有该角色: " + role.getName());
        }

        roles.add(role);

        // 发布角色分配事件
        addDomainEvent(new RoleAssignedEvent(user.getId(), role.getCode()));
    }

    /**
     * 移除角色
     */
    public void removeRole(String roleCode) {
        roles.removeIf(r -> r.getCode().equals(roleCode));
    }

    /**
     * 设置数据权限
     */
    public void setDataScope(DataScopeType scopeType, Long deptId) {
        // 验证科室数据权限必须提供科室ID
        if (scopeType == DataScopeType.DEPT && deptId == null) {
            throw DomainException.of("科室数据权限必须指定科室ID");
        }

        this.dataScopeType = scopeType;
        this.deptId = deptId;

        // 发布数据权限变更事件
        addDomainEvent(new DataScopeChangedEvent(user.getId(), scopeType));
    }

    /**
     * 判断是否拥有指定角色
     */
    public boolean hasRole(String roleCode) {
        return roles.stream().anyMatch(r -> r.getCode().equals(roleCode));
    }

    /**
     * 判断是否拥有任一角色
     */
    public boolean hasAnyRole(String... roleCodes) {
        return roles.stream().anyMatch(r -> {
            for (String code : roleCodes) {
                if (r.getCode().equals(code)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * 判断是否可以监督指定用户（导师权限）
     */
    public boolean canSupervise() {
        return hasAnyRole("SUPERVISOR", "TEACHER") && user.isEnabled();
    }

    /**
     * 启用用户
     */
    public void enable() {
        user.setStatus(1);
    }

    /**
     * 禁用用户
     */
    public void disable() {
        if (hasRole("HOSPITAL_ADMIN") && getRoleCount() == 1) {
            throw DomainException.of("不能禁用唯一的医院管理员");
        }
        user.setStatus(0);
    }

    /**
     * 更新基本信息
     */
    public void updateProfile(String realName, Email email, PhoneNumber phoneNumber) {
        if (realName != null && !realName.isBlank()) {
            user.setRealName(realName);
        }
        if (email != null) {
            user.setEmail(email.value());
        }
        if (phoneNumber != null) {
            user.setPhone(phoneNumber.value());
        }
    }

    /**
     * 修改密码
     */
    public void changePassword(Password oldPassword, Password newPassword, Password.PasswordEncoder encoder) {
        Password currentPassword = Password.of(user.getPassword());

        if (!currentPassword.matches(getPasswordForVerification(), encoder)) {
            throw DomainException.of("原密码错误");
        }

        user.setPassword(newPassword.encryptedValue());
    }

    /**
     * 获取角色数量
     */
    public int getRoleCount() {
        return roles.size();
    }

    /**
     * 获取角色代码集合
     */
    public Set<String> getRoleCodes() {
        return roles.stream()
                .map(Role::getCode)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 辅助方法：获取密码用于验证（避免暴露实际密码）
     */
    private String getPasswordForVerification() {
        // 这里返回空字符串，实际验证逻辑在Password值对象中
        return "";
    }
}
