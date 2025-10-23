package com.oncoresi.infra.persistence.mapper;

import com.mybatisflex.core.BaseMapper;
import com.oncoresi.infra.persistence.po.RolePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper（MyBatis-Flex）
 */
@Mapper
public interface RoleMapper extends BaseMapper<RolePO> {

    /**
     * 根据角色编码查询角色
     */
    @Select("SELECT * FROM sys_role WHERE code = #{code}")
    RolePO selectByCode(@Param("code") String code);

    /**
     * 根据角色编码列表查询角色列表
     */
    @Select("""
        <script>
        SELECT * FROM sys_role
        WHERE code IN
        <foreach item='code' collection='codes' open='(' separator=',' close=')'>
            #{code}
        </foreach>
        </script>
        """)
    List<RolePO> selectByCodes(@Param("codes") List<String> codes);
}
