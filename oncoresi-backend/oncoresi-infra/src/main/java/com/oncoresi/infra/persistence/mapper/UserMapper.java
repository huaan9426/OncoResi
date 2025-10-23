package com.oncoresi.infra.persistence.mapper;

import com.mybatisflex.core.BaseMapper;
import com.oncoresi.infra.persistence.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper（MyBatis-Flex）
 *
 * BaseMapper 提供了基础的 CRUD 方法：
 * - selectOneById(id)
 * - selectAll()
 * - insert(entity)
 * - update(entity)
 * - deleteById(id)
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    UserPO selectByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询其所有角色编码
     */
    @Select("""
        SELECT r.code
        FROM sys_role r
        INNER JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId}
        """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询用户及其角色
     * （注意：这是一个组合查询，返回的 UserPO 不包含 roles，需要单独查询）
     */
    default UserPO selectByIdWithRoles(Long userId) {
        return selectOneById(userId);
        // 调用方需要自行查询 roles: selectRoleCodesByUserId(userId)
    }

    /**
     * 删除用户的所有角色关联
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteUserRoles(@Param("userId") Long userId);

    /**
     * 添加用户角色关联
     */
    @org.apache.ibatis.annotations.Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
