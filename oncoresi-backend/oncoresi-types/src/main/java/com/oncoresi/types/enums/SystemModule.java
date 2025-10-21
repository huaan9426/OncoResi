package com.oncoresi.types.enums;

import lombok.Getter;

/**
 * 系统模块枚举
 * 根据需求定义的7大子系统
 */
@Getter
public enum SystemModule {

    PROCESS_MANAGEMENT("PROCESS_MANAGEMENT", "过程管理系统"),
    RECRUITMENT("RECRUITMENT", "招录系统"),
    CAPACITY_BUILDING("CAPACITY_BUILDING", "能力建设系统"),
    CLINICAL_CASE("CLINICAL_CASE", "临床病例系统"),
    SKILL_EXAM("SKILL_EXAM", "技能考试系统"),
    THEORY_EXAM("THEORY_EXAM", "理论考试系统"),
    PERFORMANCE_ANALYSIS("PERFORMANCE_ANALYSIS", "绩效分析系统");

    private final String code;
    private final String description;

    SystemModule(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
