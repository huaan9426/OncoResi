package com.oncoresi.types.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 * 根据需求定义的8种角色
 */
@Getter
public enum UserRole {

    HOSPITAL_ADMIN("HOSPITAL_ADMIN", "医院管理员"),
    BASE_ADMIN("BASE_ADMIN", "专业基地管理员"),
    DEPT_ADMIN("DEPT_ADMIN", "科室管理员"),
    SUPERVISOR("SUPERVISOR", "责任导师"),
    TEACHER("TEACHER", "带教老师"),
    TRAINEE("TRAINEE", "学员"),
    NURSE_EVALUATOR("NURSE_EVALUATOR", "护士评价"),
    PATIENT_EVALUATOR("PATIENT_EVALUATOR", "病人评价");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
