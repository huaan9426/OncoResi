package com.oncoresi.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 出勤率值对象（不可变）
 */
public record AttendanceRate(int totalDays, int attendedDays, int absentDays, int leaveDays) {

    public AttendanceRate {
        if (totalDays < 0) {
            throw new IllegalArgumentException("总天数不能为负数");
        }
        if (attendedDays < 0) {
            throw new IllegalArgumentException("出勤天数不能为负数");
        }
        if (absentDays < 0) {
            throw new IllegalArgumentException("缺勤天数不能为负数");
        }
        if (leaveDays < 0) {
            throw new IllegalArgumentException("请假天数不能为负数");
        }
        if (attendedDays + absentDays + leaveDays != totalDays) {
            throw new IllegalArgumentException("出勤天数+缺勤天数+请假天数必须等于总天数");
        }
    }

    /**
     * 计算出勤率（百分比）
     */
    public BigDecimal getRate() {
        if (totalDays == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(attendedDays)
                .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取出勤率（保留2位小数）
     */
    public BigDecimal getRateFormatted() {
        return getRate().setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 判断出勤率是否达标（默认80%）
     */
    public boolean isQualified() {
        return getRate().compareTo(BigDecimal.valueOf(80)) >= 0;
    }

    /**
     * 判断出勤率是否达到指定标准
     */
    public boolean isQualified(int threshold) {
        return getRate().compareTo(BigDecimal.valueOf(threshold)) >= 0;
    }

    /**
     * 格式化显示（出勤45/50天，出勤率90.00%）
     */
    public String formatted() {
        return String.format("出勤%d/%d天，出勤率%s%%",
                attendedDays, totalDays, getRateFormatted());
    }

    /**
     * 创建出勤率
     */
    public static AttendanceRate of(int total, int attended, int absent, int leave) {
        return new AttendanceRate(total, attended, absent, leave);
    }
}
