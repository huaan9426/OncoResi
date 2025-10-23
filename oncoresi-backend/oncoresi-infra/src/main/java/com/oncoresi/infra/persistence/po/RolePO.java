package com.oncoresi.infra.persistence.po;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色数据库持久化对象（MyBatis-Flex）
 */
@Data
@Table("sys_role")
public class RolePO {

    /**
     * 主键ID，自增
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 角色编码，唯一（如：HOSPITAL_ADMIN）
     */
    @Column("code")
    private String code;

    /**
     * 角色名称（如：医院管理员）
     */
    @Column("name")
    private String name;

    /**
     * 角色描述
     */
    @Column("description")
    private String description;

    /**
     * 创建时间（自动填充）
     */
    @Column(value = "create_time", onInsertValue = "now()")
    private LocalDateTime createTime;
}
