package com.oncoresi.domain.valueobject;

/**
 * 成绩等级枚举
 */
public enum ScoreGrade {
    /**
     * 优秀（90-100分）
     */
    EXCELLENT("优秀", 90, 100),

    /**
     * 良好（80-89分）
     */
    GOOD("良好", 80, 89),

    /**
     * 中等（70-79分）
     */
    MODERATE("中等", 70, 79),

    /**
     * 及格（60-69分）
     */
    PASS("及格", 60, 69),

    /**
     * 不及格（0-59分）
     */
    FAIL("不及格", 0, 59);

    private final String displayName;
    private final int minScore;
    private final int maxScore;

    ScoreGrade(String displayName, int minScore, int maxScore) {
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    /**
     * 根据分数获取等级
     */
    public static ScoreGrade fromScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("分数必须在0-100之间: " + score);
        }

        for (ScoreGrade grade : values()) {
            if (score >= grade.minScore && score <= grade.maxScore) {
                return grade;
            }
        }

        return FAIL; // 默认不及格
    }

    /**
     * 判断是否及格
     */
    public boolean isPassing() {
        return this != FAIL;
    }
}
