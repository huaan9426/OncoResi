package com.oncoresi.domain.event;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户注册事件
 */
public record UserRegisteredEvent(
        Long userId,
        String username,
        Set<String> roleCodes,
        LocalDateTime occurredOn
) implements DomainEvent {

    public UserRegisteredEvent(Long userId, String username, Set<String> roleCodes) {
        this(userId, username, roleCodes, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "USER_REGISTERED";
    }

    @Override
    public Long aggregateId() {
        return userId;
    }
}
