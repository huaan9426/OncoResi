package com.oncoresi.domain.event;

import java.time.LocalDateTime;

/**
 * 领域事件基础接口
 * 所有领域事件都应实现此接口
 */
public interface DomainEvent {

    /**
     * 事件发生时间
     */
    LocalDateTime occurredOn();

    /**
     * 事件类型（用于消息路由）
     */
    String eventType();

    /**
     * 事件聚合根ID（可选）
     */
    default Long aggregateId() {
        return null;
    }
}
