package com.oncoresi.domain.event;

import java.time.LocalDateTime;

/**
 * 轮转添加事件
 */
public record RotationAddedEvent(
        Long planId,
        Long rotationId,
        LocalDateTime occurredOn
) implements DomainEvent {

    public RotationAddedEvent(Long planId, Long rotationId) {
        this(planId, rotationId, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ROTATION_ADDED";
    }

    @Override
    public Long aggregateId() {
        return planId;
    }
}
