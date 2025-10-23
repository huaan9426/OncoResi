package com.oncoresi.domain.event;

import com.oncoresi.infra.security.DataScopeContext.DataScopeType;
import java.time.LocalDateTime;

/**
 * 数据权限变更事件
 */
public record DataScopeChangedEvent(
        Long userId,
        DataScopeType newScopeType,
        LocalDateTime occurredOn
) implements DomainEvent {

    public DataScopeChangedEvent(Long userId, DataScopeType newScopeType) {
        this(userId, newScopeType, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "DATA_SCOPE_CHANGED";
    }

    @Override
    public Long aggregateId() {
        return userId;
    }
}
