package com.oncoresi.application.event.handler;

import com.oncoresi.domain.event.PhaseCompletedEvent;
import com.oncoresi.domain.event.RotationAddedEvent;
import com.oncoresi.domain.event.TrainingPlanCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 培训事件处理器
 */
@Slf4j
@RequiredArgsConstructor
public class TrainingEventHandler {

    /**
     * 处理培训计划创建事件
     */
    @Component
    @ConditionalOnProperty(name = "rocketmq.name-server")
    @RocketMQMessageListener(
            topic = "domain-events",
            selectorExpression = "TRAINING_PLAN_CREATED",
            consumerGroup = "training-plan-consumer"
    )
    public static class TrainingPlanCreatedEventHandler implements RocketMQListener<TrainingPlanCreatedEvent> {

        @Override
        public void onMessage(TrainingPlanCreatedEvent event) {
            log.info("处理培训计划创建事件: planId={}, planName={}", event.planId(), event.planName());

            // 这里可以执行：
            // 1. 通知相关人员（导师、学员）
            // 2. 初始化计划相关数据
            // 3. 生成统计报表基础数据

            log.info("培训计划创建事件处理完成: planId={}", event.planId());
        }
    }

    /**
     * 处理轮转添加事件
     */
    @Component
    @ConditionalOnProperty(name = "rocketmq.name-server")
    @RocketMQMessageListener(
            topic = "domain-events",
            selectorExpression = "ROTATION_ADDED",
            consumerGroup = "rotation-consumer"
    )
    public static class RotationAddedEventHandler implements RocketMQListener<RotationAddedEvent> {

        @Override
        public void onMessage(RotationAddedEvent event) {
            log.info("处理轮转添加事件: planId={}, rotationId={}", event.planId(), event.rotationId());

            // 这里可以执行：
            // 1. 通知学员轮转安排
            // 2. 通知科室准备接收学员
            // 3. 更新学员日程表

            log.info("轮转添加事件处理完成: rotationId={}", event.rotationId());
        }
    }

    /**
     * 处理阶段完成事件
     */
    @Component
    @ConditionalOnProperty(name = "rocketmq.name-server")
    @RocketMQMessageListener(
            topic = "domain-events",
            selectorExpression = "PHASE_COMPLETED",
            consumerGroup = "phase-consumer"
    )
    public static class PhaseCompletedEventHandler implements RocketMQListener<PhaseCompletedEvent> {

        @Override
        public void onMessage(PhaseCompletedEvent event) {
            log.info("处理阶段完成事件: planId={}, phaseName={}", event.planId(), event.phaseName());

            // 这里可以执行：
            // 1. 生成阶段总结报告
            // 2. 通知学员和导师
            // 3. 更新绩效统计数据
            // 4. 触发下一阶段开始

            log.info("阶段完成事件处理完成: planId={}, phase={}", event.planId(), event.phaseName());
        }
    }
}
