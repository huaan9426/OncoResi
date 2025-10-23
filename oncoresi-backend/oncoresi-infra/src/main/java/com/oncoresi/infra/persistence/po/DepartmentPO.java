package com.oncoresi.infra.persistence.po;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 科室持久化对象
 */
@Data
@Table("sys_department")
public class DepartmentPO {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column("hospital_id")
    private Long hospitalId;

    @Column("dept_name")
    private String deptName;

    @Column("dept_code")
    private String deptCode;

    @Column("dept_type")
    private String deptType;

    @Column("director_id")
    private Long directorId;

    @Column("description")
    private String description;

    @Column(value = "create_time", onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(value = "update_time", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;
}
