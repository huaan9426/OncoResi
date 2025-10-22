package com.oncoresi.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色领域实体
 */
@Data
public class Role {

    private Long id;

    /** 角色代码(如: HOSPITAL_ADMIN) */
    private String code;

    /** 角色名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;
}
