package com.oncoresi.application.event.handler;

import com.oncoresi.domain.event.RoleAssignedEvent;
import com.oncoresi.domain.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 用户事件处理器
 */
@Slf4j
@RequiredArgsConstructor
public class UserEventHandler {

    /**
     * 处理用户注册事件
     */
    @Component
    @ConditionalOnProperty(name = "rocketmq.name-server")
    @RocketMQMessageListener(
            topic = "domain-events",
            selectorExpression = "USER_REGISTERED",
            consumerGroup = "user-event-consumer"
    )
    public static class UserRegisteredEventHandler implements RocketMQListener<UserRegisteredEvent> {

        @Override
        public void onMessage(UserRegisteredEvent event) {
            log.info("处理用户注册事件: userId={}, username={}", event.userId(), event.username());

            // 这里可以执行异步任务，例如：
            // 1. 发送欢迎邮件
            // 2. 初始化用户默认设置
            // 3. 记录审计日志
            // 4. 通知相关人员

            log.info("用户注册事件处理完成: userId={}", event.userId());
        }
    }

    /**
     * 处理角色分配事件
     */
    @Component
    @ConditionalOnProperty(name = "rocketmq.name-server")
    @RocketMQMessageListener(
            topic = "domain-events",
            selectorExpression = "ROLE_ASSIGNED",
            consumerGroup = "role-event-consumer"
    )
    public static class RoleAssignedEventHandler implements RocketMQListener<RoleAssignedEvent> {

        @Override
        public void onMessage(RoleAssignedEvent event) {
            log.info("处理角色分配事件: userId={}, roleCode={}", event.userId(), event.roleCode());

            // 这里可以执行：
            // 1. 刷新用户权限缓存
            // 2. 通知用户角色变更
            // 3. 记录权限变更日志

            log.info("角色分配事件处理完成: userId={}", event.userId());
        }
    }
}
