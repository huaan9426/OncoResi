package com.oncoresi.domain.aggregate;

import com.oncoresi.domain.event.ExamCompletedEvent;
import com.oncoresi.domain.exception.DomainException;
import com.oncoresi.domain.valueobject.ExamScore;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 考试聚合根
 * 封装考试、试卷、答卷、成绩等业务逻辑
 */
@Getter
public class ExamAggregate extends AggregateRoot<Long> {

    /**
     * 考试ID
     */
    private Long id;

    /**
     * 考试名称
     */
    private String examName;

    /**
     * 考试类型（SKILL-技能考试, THEORY-理论考试）
     */
    private ExamType examType;

    /**
     * 及格分数
     */
    private int passingScore;

    /**
     * 考试开始时间
     */
    private LocalDateTime startTime;

    /**
     * 考试结束时间
     */
    private LocalDateTime endTime;

    /**
     * 考试状态
     */
    private ExamStatus status;

    /**
     * 答卷列表
     */
    private final Map<Long, ExamAnswer> answers; // traineeId -> ExamAnswer

    /**
     * 成绩列表
     */
    private final Map<Long, ExamScore> scores; // traineeId -> ExamScore

    /**
     * 考试类型枚举
     */
    public enum ExamType {
        SKILL, THEORY
    }

    /**
     * 考试状态枚举
     */
    public enum ExamStatus {
        DRAFT, PUBLISHED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    /**
     * 答卷（聚合内实体）
     */
    @Getter
    public static class ExamAnswer {
        private Long traineeId;
        private List<String> answers;
        private LocalDateTime submitTime;
        private boolean submitted;

        public ExamAnswer(Long traineeId) {
            this.traineeId = traineeId;
            this.answers = new ArrayList<>();
            this.submitted = false;
        }

        public void submit(List<String> answers) {
            if (submitted) {
                throw DomainException.of("答卷已提交，无法重复提交");
            }
            this.answers = new ArrayList<>(answers);
            this.submitTime = LocalDateTime.now();
            this.submitted = true;
        }
    }

    /**
     * 私有构造函数
     */
    private ExamAggregate(Long id, String examName, ExamType examType, int passingScore,
                          LocalDateTime startTime, LocalDateTime endTime, ExamStatus status) {
        this.id = id;
        this.examName = Objects.requireNonNull(examName, "考试名称不能为空");
        this.examType = Objects.requireNonNull(examType, "考试类型不能为空");
        this.passingScore = passingScore;
        this.startTime = Objects.requireNonNull(startTime, "开始时间不能为空");
        this.endTime = Objects.requireNonNull(endTime, "结束时间不能为空");
        this.status = status != null ? status : ExamStatus.DRAFT;
        this.answers = new HashMap<>();
        this.scores = new HashMap<>();

        if (passingScore < 0 || passingScore > 100) {
            throw DomainException.of("及格分数必须在0-100之间");
        }

        if (endTime.isBefore(startTime)) {
            throw DomainException.of("结束时间不能早于开始时间");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * 重建聚合
     */
    public static ExamAggregate reconstitute(Long id, String examName, ExamType examType, int passingScore,
                                              LocalDateTime startTime, LocalDateTime endTime, ExamStatus status) {
        return new ExamAggregate(id, examName, examType, passingScore, startTime, endTime, status);
    }

    /**
     * 创建新考试
     */
    public static ExamAggregate create(String examName, ExamType examType, int passingScore,
                                        LocalDateTime startTime, LocalDateTime endTime) {
        return new ExamAggregate(null, examName, examType, passingScore, startTime, endTime, ExamStatus.DRAFT);
    }

    /**
     * 提交答卷
     */
    public void submitAnswer(Long traineeId, List<String> answers, int calculatedScore) {
        if (isExpired()) {
            throw DomainException.of("考试已结束，无法提交答卷");
        }

        if (status != ExamStatus.IN_PROGRESS) {
            throw DomainException.of("考试未开始或已结束");
        }

        // 创建或获取答卷
        ExamAnswer answer = this.answers.computeIfAbsent(traineeId, ExamAnswer::new);
        answer.submit(answers);

        // 计算成绩
        ExamScore score = ExamScore.of(calculatedScore, passingScore);
        this.scores.put(traineeId, score);

        // 发布考试完成事件
        addDomainEvent(new ExamCompletedEvent(this.id, traineeId, score));
    }

    /**
     * 发布考试
     */
    public void publish() {
        if (status != ExamStatus.DRAFT) {
            throw DomainException.of("只有草稿状态的考试可以发布");
        }

        this.status = ExamStatus.PUBLISHED;
    }

    /**
     * 开始考试
     */
    public void start() {
        if (status != ExamStatus.PUBLISHED) {
            throw DomainException.of("只有已发布的考试可以开始");
        }

        if (LocalDateTime.now().isBefore(startTime)) {
            throw DomainException.of("考试尚未到开始时间");
        }

        this.status = ExamStatus.IN_PROGRESS;
    }

    /**
     * 结束考试
     */
    public void complete() {
        if (status != ExamStatus.IN_PROGRESS) {
            throw DomainException.of("只有进行中的考试可以结束");
        }

        this.status = ExamStatus.COMPLETED;
    }

    /**
     * 判断考试是否已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }

    /**
     * 获取通过率
     */
    public double getPassRate() {
        if (scores.isEmpty()) {
            return 0.0;
        }

        long passedCount = scores.values().stream()
                .filter(ExamScore::passed)
                .count();

        return (passedCount * 100.0) / scores.size();
    }

    /**
     * 设置ID（用于持久化后）
     */
    public void setId(Long id) {
        this.id = id;
    }
}
