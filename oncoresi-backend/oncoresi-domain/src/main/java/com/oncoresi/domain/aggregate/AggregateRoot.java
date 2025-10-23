package com.oncoresi.domain.aggregate;

import com.oncoresi.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * 提供领域事件管理功能
 */
public abstract class AggregateRoot<ID> {

    /**
     * 领域事件列表（在聚合内部产生的事件）
     */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 获取聚合根ID
     */
    public abstract ID getId();

    /**
     * 添加领域事件
     */
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * 获取所有领域事件（只读）
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 清除所有领域事件
     * 通常在事件发布后调用
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * 判断是否有未发布的领域事件
     */
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }
}
