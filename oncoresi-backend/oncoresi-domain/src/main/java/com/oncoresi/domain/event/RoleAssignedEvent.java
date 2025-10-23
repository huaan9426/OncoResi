package com.oncoresi.domain.event;

import java.time.LocalDateTime;

/**
 * 角色分配事件
 */
public record RoleAssignedEvent(
        Long userId,
        String roleCode,
        LocalDateTime occurredOn
) implements DomainEvent {

    public RoleAssignedEvent(Long userId, String roleCode) {
        this(userId, roleCode, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "ROLE_ASSIGNED";
    }

    @Override
    public Long aggregateId() {
        return userId;
    }
}
