package com.oncoresi.infra.persistence.mapper;

import com.mybatisflex.core.BaseMapper;
import com.oncoresi.infra.persistence.po.DepartmentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 科室 Mapper
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<DepartmentPO> {

    /**
     * 根据医院ID查询所有科室
     */
    @Select("SELECT * FROM sys_department WHERE hospital_id = #{hospitalId}")
    List<DepartmentPO> selectByHospitalId(@Param("hospitalId") Long hospitalId);
}
