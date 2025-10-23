package com.oncoresi.domain.event;

import java.time.LocalDateTime;

/**
 * 培训阶段完成事件
 */
public record PhaseCompletedEvent(
        Long planId,
        String phaseName,
        LocalDateTime occurredOn
) implements DomainEvent {

    public PhaseCompletedEvent(Long planId, String phaseName) {
        this(planId, phaseName, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "PHASE_COMPLETED";
    }

    @Override
    public Long aggregateId() {
        return planId;
    }
}
