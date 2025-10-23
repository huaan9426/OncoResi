package com.oncoresi.domain.valueobject;

/**
 * 考试成绩值对象（不可变）
 */
public record ExamScore(
        int score,
        boolean passed,
        ScoreGrade grade
) {

    public ExamScore {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("分数必须在0-100之间: " + score);
        }
    }

    /**
     * 根据分数和及格线创建成绩
     */
    public static ExamScore of(int score, int passingScore) {
        ScoreGrade grade = ScoreGrade.fromScore(score);
        boolean passed = score >= passingScore;
        return new ExamScore(score, passed, grade);
    }

    /**
     * 创建满分成绩
     */
    public static ExamScore perfect() {
        return new ExamScore(100, true, ScoreGrade.EXCELLENT);
    }

    /**
     * 创建不及格成绩
     */
    public static ExamScore failed(int score) {
        return new ExamScore(score, false, ScoreGrade.fromScore(score));
    }

    /**
     * 获取等级显示名称
     */
    public String getGradeName() {
        return grade.getDisplayName();
    }

    /**
     * 判断是否优秀
     */
    public boolean isExcellent() {
        return grade == ScoreGrade.EXCELLENT;
    }

    /**
     * 判断是否良好及以上
     */
    public boolean isGoodOrBetter() {
        return grade == ScoreGrade.EXCELLENT || grade == ScoreGrade.GOOD;
    }

    /**
     * 格式化显示（85分/良好/通过）
     */
    public String formatted() {
        return String.format("%d分/%s/%s",
                score,
                grade.getDisplayName(),
                passed ? "通过" : "未通过");
    }
}
