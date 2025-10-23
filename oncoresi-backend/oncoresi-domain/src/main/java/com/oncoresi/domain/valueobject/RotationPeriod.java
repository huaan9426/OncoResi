package com.oncoresi.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 轮转周期值对象（不可变）
 * 代表学员在某科室的轮转时间段
 */
public record RotationPeriod(LocalDate startDate, LocalDate endDate) {

    public RotationPeriod {
        Objects.requireNonNull(startDate, "开始日期不能为空");
        Objects.requireNonNull(endDate, "结束日期不能为空");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
    }

    public static RotationPeriod of(LocalDate start, LocalDate end) {
        return new RotationPeriod(start, end);
    }

    /**
     * 判断轮转周期是否与另一个周期重叠
     */
    public boolean overlaps(RotationPeriod other) {
        return !this.endDate.isBefore(other.startDate) &&
               !this.startDate.isAfter(other.endDate);
    }

    /**
     * 判断轮转是否正在进行
     */
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    /**
     * 判断轮转是否已结束
     */
    public boolean isCompleted() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * 获取轮转持续天数
     */
    public long getDurationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * 获取已轮转天数
     */
    public long getElapsedDays() {
        if (isCompleted()) {
            return getDurationDays();
        }
        if (!isActive()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1;
    }

    /**
     * 获取剩余天数
     */
    public long getRemainingDays() {
        if (isCompleted()) {
            return 0;
        }
        if (!isActive()) {
            return getDurationDays();
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }
}
