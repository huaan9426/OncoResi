package com.oncoresi.infra.persistence.mapper;

import com.mybatisflex.core.BaseMapper;
import com.oncoresi.infra.persistence.po.DataScopePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 数据权限范围 Mapper
 */
@Mapper
public interface DataScopeMapper extends BaseMapper<DataScopePO> {

    /**
     * 根据用户ID查询数据权限
     */
    @Select("SELECT * FROM sys_user_data_scope WHERE user_id = #{userId} LIMIT 1")
    DataScopePO selectByUserId(@Param("userId") Long userId);
}
