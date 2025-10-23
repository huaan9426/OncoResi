package com.oncoresi.application.event;

import com.oncoresi.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 领域事件发布器
 * 使用 RocketMQ 发布领域事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server")
public class DomainEventPublisher {

    private static final String TOPIC = "domain-events";

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 发布领域事件
     */
    public void publish(DomainEvent event) {
        try {
            String destination = TOPIC + ":" + event.eventType();

            rocketMQTemplate.syncSend(
                    destination,
                    MessageBuilder.withPayload(event).build()
            );

            log.info("领域事件已发布: type={}, aggregateId={}",
                    event.eventType(), event.aggregateId());

        } catch (Exception e) {
            log.error("领域事件发布失败: type={}", event.eventType(), e);
            // 在实际生产环境中，可以将失败的事件存储到数据库，稍后重试
        }
    }

    /**
     * 批量发布领域事件
     */
    public void publishAll(Iterable<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
