package com.oncoresi.domain.valueobject;

/**
 * 课程类别枚举
 */
public enum CourseCategory {
    /**
     * 基础理论课程
     */
    BASIC_THEORY("基础理论"),

    /**
     * 专业理论课程
     */
    PROFESSIONAL_THEORY("专业理论"),

    /**
     * 临床技能课程
     */
    CLINICAL_SKILLS("临床技能"),

    /**
     * 手术技能课程
     */
    SURGICAL_SKILLS("手术技能"),

    /**
     * 医患沟通课程
     */
    COMMUNICATION("医患沟通"),

    /**
     * 医学伦理课程
     */
    MEDICAL_ETHICS("医学伦理"),

    /**
     * 急救技能课程
     */
    EMERGENCY_SKILLS("急救技能"),

    /**
     * 科研方法课程
     */
    RESEARCH_METHOD("科研方法"),

    /**
     * 其他课程
     */
    OTHER("其他");

    private final String displayName;

    CourseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 判断是否为技能类课程
     */
    public boolean isSkillBased() {
        return this == CLINICAL_SKILLS ||
               this == SURGICAL_SKILLS ||
               this == EMERGENCY_SKILLS;
    }

    /**
     * 判断是否为理论类课程
     */
    public boolean isTheoryBased() {
        return this == BASIC_THEORY ||
               this == PROFESSIONAL_THEORY ||
               this == MEDICAL_ETHICS ||
               this == RESEARCH_METHOD;
    }

    /**
     * 判断是否为必修课程（基础理论和专业理论）
     */
    public boolean isRequired() {
        return this == BASIC_THEORY || this == PROFESSIONAL_THEORY;
    }
}
