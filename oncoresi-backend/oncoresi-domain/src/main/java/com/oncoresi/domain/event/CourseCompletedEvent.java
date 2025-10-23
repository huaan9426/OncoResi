package com.oncoresi.domain.event;

import java.time.LocalDateTime;

/**
 * 课程完成事件
 */
public record CourseCompletedEvent(
        Long courseId,
        Long traineeId,
        LocalDateTime occurredOn
) implements DomainEvent {

    public CourseCompletedEvent(Long courseId, Long traineeId) {
        this(courseId, traineeId, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "COURSE_COMPLETED";
    }

    @Override
    public Long aggregateId() {
        return courseId;
    }
}
