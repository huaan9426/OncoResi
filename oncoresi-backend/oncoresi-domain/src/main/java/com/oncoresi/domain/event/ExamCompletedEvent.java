package com.oncoresi.domain.event;

import com.oncoresi.domain.valueobject.ExamScore;
import java.time.LocalDateTime;

/**
 * 考试完成事件
 */
public record ExamCompletedEvent(
        Long examId,
        Long traineeId,
        ExamScore score,
        LocalDateTime occurredOn
) implements DomainEvent {

    public ExamCompletedEvent(Long examId, Long traineeId, ExamScore score) {
        this(examId, traineeId, score, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "EXAM_COMPLETED";
    }

    @Override
    public Long aggregateId() {
        return examId;
    }
}
