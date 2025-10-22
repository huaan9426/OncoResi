package com.oncoresi.infra.persistence.converter;

import com.oncoresi.domain.entity.Role;
import com.oncoresi.domain.entity.User;
import com.oncoresi.infra.persistence.po.RolePO;
import com.oncoresi.infra.persistence.po.UserPO;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户领域对象与PO转换器
 */
public class UserConverter {

    /**
     * PO转领域对象
     */
    public static User toDomain(UserPO po) {
        if (po == null) {
            return null;
        }

        User user = new User();
        user.setId(po.getId());
        user.setUsername(po.getUsername());
        user.setPassword(po.getPassword());
        user.setRealName(po.getRealName());
        user.setPhone(po.getPhone());
        user.setEmail(po.getEmail());
        user.setStatus(po.getStatus());
        user.setCreateTime(po.getCreateTime());
        user.setUpdateTime(po.getUpdateTime());

        // 转换角色
        if (po.getRoles() != null) {
            Set<Role> roles = po.getRoles().stream()
                    .map(UserConverter::roleToDomain)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        return user;
    }

    /**
     * 领域对象转PO
     */
    public static UserPO toPO(User user) {
        if (user == null) {
            return null;
        }

        UserPO po = new UserPO();
        po.setId(user.getId());
        po.setUsername(user.getUsername());
        po.setPassword(user.getPassword());
        po.setRealName(user.getRealName());
        po.setPhone(user.getPhone());
        po.setEmail(user.getEmail());
        po.setStatus(user.getStatus());
        po.setCreateTime(user.getCreateTime());
        po.setUpdateTime(user.getUpdateTime());

        return po;
    }

    /**
     * RolePO转Role
     */
    private static Role roleToDomain(RolePO po) {
        if (po == null) {
            return null;
        }

        Role role = new Role();
        role.setId(po.getId());
        role.setCode(po.getCode());
        role.setName(po.getName());
        role.setDescription(po.getDescription());
        role.setCreateTime(po.getCreateTime());

        return role;
    }
}
