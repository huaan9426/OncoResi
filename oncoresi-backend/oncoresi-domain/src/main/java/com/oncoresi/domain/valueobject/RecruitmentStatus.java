package com.oncoresi.domain.valueobject;

/**
 * 招录状态枚举
 */
public enum RecruitmentStatus {
    /**
     * 报名中
     */
    REGISTERING("报名中"),

    /**
     * 报名截止
     */
    REGISTRATION_CLOSED("报名截止"),

    /**
     * 审核中
     */
    UNDER_REVIEW("审核中"),

    /**
     * 审核通过
     */
    REVIEW_PASSED("审核通过"),

    /**
     * 审核拒绝
     */
    REVIEW_REJECTED("审核拒绝"),

    /**
     * 待考试
     */
    PENDING_EXAM("待考试"),

    /**
     * 已录取
     */
    ADMITTED("已录取"),

    /**
     * 未录取
     */
    NOT_ADMITTED("未录取"),

    /**
     * 已撤销
     */
    CANCELLED("已撤销");

    private final String displayName;

    RecruitmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 判断是否可以继续后续流程
     */
    public boolean canProceed() {
        return this == REVIEW_PASSED || this == PENDING_EXAM;
    }

    /**
     * 判断是否已结束
     */
    public boolean isFinalized() {
        return this == ADMITTED || this == NOT_ADMITTED || this == CANCELLED;
    }

    /**
     * 判断是否需要审核
     */
    public boolean needsReview() {
        return this == REGISTERING || this == UNDER_REVIEW;
    }
}
