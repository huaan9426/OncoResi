package com.oncoresi.domain.event;

import java.time.LocalDateTime;

/**
 * 报名申请审核事件
 */
public record ApplicationReviewedEvent(
        Long applicationId,
        boolean approved,
        LocalDateTime occurredOn
) implements DomainEvent {

    public ApplicationReviewedEvent(Long applicationId, boolean approved) {
        this(applicationId, approved, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "APPLICATION_REVIEWED";
    }

    @Override
    public Long aggregateId() {
        return applicationId;
    }
}
