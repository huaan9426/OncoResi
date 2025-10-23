package com.oncoresi.domain.event;

import java.time.LocalDateTime;

/**
 * 培训计划创建事件
 */
public record TrainingPlanCreatedEvent(
        Long planId,
        String planName,
        LocalDateTime occurredOn
) implements DomainEvent {

    public TrainingPlanCreatedEvent(Long planId, String planName) {
        this(planId, planName, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "TRAINING_PLAN_CREATED";
    }

    @Override
    public Long aggregateId() {
        return planId;
    }
}
