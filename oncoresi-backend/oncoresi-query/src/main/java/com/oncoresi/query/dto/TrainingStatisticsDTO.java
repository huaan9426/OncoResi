package com.oncoresi.query.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 培训统计数据DTO（查询模型）
 * 用于绩效分析和报表展示
 */
@Data
public class TrainingStatisticsDTO {

    /**
     * 学员ID
     */
    private Long traineeId;

    /**
     * 学员姓名
     */
    private String traineeName;

    /**
     * 科室名称
     */
    private String departmentName;

    /**
     * 出勤天数
     */
    private Integer attendanceCount;

    /**
     * 总天数
     */
    private Integer totalDays;

    /**
     * 出勤率（百分比）
     */
    private BigDecimal attendanceRate;

    /**
     * 考试次数
     */
    private Integer examCount;

    /**
     * 考试平均分
     */
    private BigDecimal avgExamScore;

    /**
     * 考试通过率（百分比）
     */
    private BigDecimal examPassRate;

    /**
     * 课程完成数
     */
    private Integer completedCourseCount;

    /**
     * 总课程数
     */
    private Integer totalCourseCount;

    /**
     * 课程完成率（百分比）
     */
    private BigDecimal courseCompletionRate;

    /**
     * 综合评分（基于各项指标计算）
     */
    private BigDecimal overallScore;

    /**
     * 评级（优秀/良好/中等/及格/不及格）
     */
    private String rating;
}
