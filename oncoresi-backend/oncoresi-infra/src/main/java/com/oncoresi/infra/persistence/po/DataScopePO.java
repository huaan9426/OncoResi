package com.oncoresi.infra.persistence.po;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据权限范围持久化对象
 */
@Data
@Table("sys_user_data_scope")
public class DataScopePO {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column("user_id")
    private Long userId;

    /**
     * 权限类型: ALL(全院), DEPT(科室), SUPERVISED(带教学员), SELF(个人)
     */
    @Column("scope_type")
    private String scopeType;

    @Column("dept_id")
    private Long deptId;

    @Column(value = "create_time", onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(value = "update_time", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;
}
