package com.oncoresi.application.event.handler;

import com.oncoresi.domain.event.ExamCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 考试事件处理器
 */
@Slf4j
@RequiredArgsConstructor
public class ExamEventHandler {

    /**
     * 处理考试完成事件
     */
    @Component
    @ConditionalOnProperty(name = "rocketmq.name-server")
    @RocketMQMessageListener(
            topic = "domain-events",
            selectorExpression = "EXAM_COMPLETED",
            consumerGroup = "exam-consumer"
    )
    public static class ExamCompletedEventHandler implements RocketMQListener<ExamCompletedEvent> {

        @Override
        public void onMessage(ExamCompletedEvent event) {
            log.info("处理考试完成事件: examId={}, traineeId={}, score={}",
                    event.examId(), event.traineeId(), event.score().score());

            // 这里可以执行：
            // 1. 更新学员成绩统计
            // 2. 通知学员考试结果
            // 3. 如果不及格，安排补考
            // 4. 更新绩效分析数据（读模型）

            if (!event.score().passed()) {
                log.warn("学员考试未通过: traineeId={}, score={}",
                        event.traineeId(), event.score().score());
                // 触发补考安排流程
            }

            log.info("考试完成事件处理完成: examId={}", event.examId());
        }
    }
}
