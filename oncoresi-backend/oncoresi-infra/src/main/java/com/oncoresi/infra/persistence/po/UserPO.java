package com.oncoresi.infra.persistence.po;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据库持久化对象（MyBatis-Flex）
 */
@Data
@Table("sys_user")
public class UserPO {

    /**
     * 主键ID，自增
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户名，唯一
     */
    @Column("username")
    private String username;

    /**
     * 密码（BCrypt 加密）
     */
    @Column("password")
    private String password;

    /**
     * 真实姓名
     */
    @Column("real_name")
    private String realName;

    /**
     * 手机号
     */
    @Column("phone")
    private String phone;

    /**
     * 邮箱
     */
    @Column("email")
    private String email;

    /**
     * 状态：1-启用，0-禁用
     */
    @Column("status")
    private Integer status = 1;

    /**
     * 创建时间（自动填充）
     */
    @Column(value = "create_time", onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @Column(value = "update_time", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;

    // 注意：roles 不再作为实体字段，而是通过 Mapper 查询关联数据
}
