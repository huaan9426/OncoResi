package com.oncoresi.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * AI辅助服务（Spring AI + Ollama）
 * 提供培训报告生成和智能问答功能
 * <p>
 * 注意：此服务仅在配置 spring.ai.enabled=true 时加载
 * 如果未启用 AI，此 Bean 不会被创建，不影响系统其他功能
 *
 * @author OncoResi Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.ai.enabled", havingValue = "true")
public class ClinicalCaseAnalysisService {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * 生成培训报告摘要
     *
     * @param attendanceRate 出勤率
     * @param caseCount      病例数
     * @param avgScore       平均成绩
     * @param skillPassRate  技能通过率
     * @return 培训报告摘要
     */
    public String generateTrainingReport(int attendanceRate, int caseCount, double avgScore, int skillPassRate) {
        log.info("开始生成培训报告: 出勤率={}%, 病例数={}", attendanceRate, caseCount);

        String prompt = String.format("""
                请根据以下学员培训数据生成一份专业的培训评估报告摘要：

                【出勤率】%d%%
                【病例提交数】%d 例
                【考试平均分】%.1f 分
                【技能考核通过率】%d%%

                请生成一份简洁的评估报告，包含：
                1. 整体表现评价
                2. 优势与亮点
                3. 需要改进的地方
                4. 下一步建议

                报告要客观、专业、有建设性。
                """, attendanceRate, caseCount, avgScore, skillPassRate);

        try {
            String response = chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("培训报告生成完成");
            return response;

        } catch (Exception e) {
            log.error("培训报告生成失败", e);
            return "报告生成服务暂时不可用: " + e.getMessage();
        }
    }

    /**
     * 智能问答助手
     *
     * @param question 医学相关问题
     * @return AI 回答
     */
    public String medicalQA(String question) {
        log.info("医学问答: {}", question);

        String prompt = String.format("""
                作为医学培训助手，请回答以下问题：

                %s

                要求：
                - 答案准确、专业
                - 语言简洁易懂
                - 如涉及诊疗建议，需提醒"仅供参考，具体以临床实际为准"
                """, question);

        try {
            return chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("医学问答失败", e);
            return "问答服务暂时不可用: " + e.getMessage();
        }
    }
}
