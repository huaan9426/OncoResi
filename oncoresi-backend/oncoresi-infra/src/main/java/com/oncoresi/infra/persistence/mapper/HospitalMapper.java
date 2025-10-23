package com.oncoresi.infra.persistence.mapper;

import com.mybatisflex.core.BaseMapper;
import com.oncoresi.infra.persistence.po.HospitalPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医院 Mapper
 */
@Mapper
public interface HospitalMapper extends BaseMapper<HospitalPO> {
}
