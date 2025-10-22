package com.oncoresi.infra.persistence.po;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 角色数据库持久化对象
 */
@Data
@Entity
@Table(name = "sys_role")
public class RolePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}
