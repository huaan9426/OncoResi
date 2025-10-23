package com.oncoresi.infra.persistence.converter;

import com.oncoresi.domain.entity.Role;
import com.oncoresi.domain.entity.User;
import com.oncoresi.infra.persistence.po.RolePO;
import com.oncoresi.infra.persistence.po.UserPO;
import org.springframework.stereotype.Component;

/**
 * 用户领域对象与 PO 转换器（MyBatis-Flex）
 *
 * @author OncoResi Team
 */
@Component
public class UserConverter {

    /**
     * PO 转领域对象（不包含角色，角色需单独查询和设置）
     */
    public User toDomain(UserPO po) {
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

        // 注意：roles 不在此处设置，由 Repository 层单独查询后设置

        return user;
    }

    /**
     * 领域对象转 PO（不包含角色，角色关系由关联表管理）
     */
    public UserPO toPO(User user) {
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
     * RolePO 转 Role 领域对象
     */
    public Role roleToDomain(RolePO po) {
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

    /**
     * Role 领域对象转 RolePO
     */
    public RolePO roleToPO(Role role) {
        if (role == null) {
            return null;
        }

        RolePO po = new RolePO();
        po.setId(role.getId());
        po.setCode(role.getCode());
        po.setName(role.getName());
        po.setDescription(role.getDescription());
        po.setCreateTime(role.getCreateTime());

        return po;
    }
}

