package com.oncoresi.infra.persistence.po;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医院持久化对象
 */
@Data
@Table("sys_hospital")
public class HospitalPO {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column("hospital_name")
    private String hospitalName;

    @Column("hospital_code")
    private String hospitalCode;

    @Column("address")
    private String address;

    @Column("contact_phone")
    private String contactPhone;

    @Column("director")
    private String director;

    @Column(value = "create_time", onInsertValue = "now()")
    private LocalDateTime createTime;

    @Column(value = "update_time", onInsertValue = "now()", onUpdateValue = "now()")
    private LocalDateTime updateTime;
}
