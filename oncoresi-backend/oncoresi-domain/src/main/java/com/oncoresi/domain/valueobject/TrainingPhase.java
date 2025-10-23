package com.oncoresi.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 培训阶段值对象（不可变）
 * 代表一个培训阶段，包含名称、时间范围和要求
 */
public record TrainingPhase(
        String phaseName,
        LocalDate startDate,
        LocalDate endDate,
        int requiredCaseCount,
        int requiredSkillCount
) {

    public TrainingPhase {
        Objects.requireNonNull(phaseName, "阶段名称不能为空");
        Objects.requireNonNull(startDate, "开始日期不能为空");
        Objects.requireNonNull(endDate, "结束日期不能为空");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        if (requiredCaseCount < 0) {
            throw new IllegalArgumentException("要求病例数不能为负数");
        }

        if (requiredSkillCount < 0) {
            throw new IllegalArgumentException("要求技能数不能为负数");
        }
    }

    /**
     * 判断阶段是否正在进行
     */
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    /**
     * 判断阶段是否已过期
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * 判断阶段是否未开始
     */
    public boolean isUpcoming() {
        return LocalDate.now().isBefore(startDate);
    }

    /**
     * 获取阶段持续天数
     */
    public long getDurationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * 计算已完成百分比
     */
    public double getCompletionPercentage(int completedCaseCount, int completedSkillCount) {
        if (requiredCaseCount == 0 && requiredSkillCount == 0) {
            return 100.0;
        }

        double casePercentage = requiredCaseCount > 0 ?
                Math.min(100.0, (completedCaseCount * 100.0) / requiredCaseCount) : 0;
        double skillPercentage = requiredSkillCount > 0 ?
                Math.min(100.0, (completedSkillCount * 100.0) / requiredSkillCount) : 0;

        return (casePercentage + skillPercentage) / 2;
    }
}
