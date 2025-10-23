package com.oncoresi.query.service;

import com.oncoresi.query.dto.TrainingStatisticsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 培训统计查询服务（CQRS - 读模型）
 * 使用原生SQL进行复杂查询，不走领域模型
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingStatisticsQueryService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 查询学员培训统计数据
     */
    public TrainingStatisticsDTO getTraineeStatistics(Long traineeId) {
        String sql = """
            SELECT
                u.id AS traineeId,
                u.real_name AS traineeName,
                d.dept_name AS departmentName,
                0 AS attendanceCount,
                0 AS totalDays,
                0.00 AS attendanceRate,
                0 AS examCount,
                0.00 AS avgExamScore,
                0.00 AS examPassRate,
                0 AS completedCourseCount,
                0 AS totalCourseCount,
                0.00 AS courseCompletionRate,
                0.00 AS overallScore,
                '待评价' AS rating
            FROM sys_user u
            LEFT JOIN sys_department d ON u.dept_id = d.id
            WHERE u.id = ?
            """;

        List<TrainingStatisticsDTO> results = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(TrainingStatisticsDTO.class),
                traineeId
        );

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 查询科室所有学员的统计数据
     */
    public List<TrainingStatisticsDTO> getDepartmentStatistics(Long departmentId) {
        String sql = """
            SELECT
                u.id AS traineeId,
                u.real_name AS traineeName,
                d.dept_name AS departmentName,
                0 AS attendanceCount,
                0 AS totalDays,
                0.00 AS attendanceRate,
                0 AS examCount,
                0.00 AS avgExamScore,
                0.00 AS examPassRate,
                0 AS completedCourseCount,
                0 AS totalCourseCount,
                0.00 AS courseCompletionRate,
                0.00 AS overallScore,
                '待评价' AS rating
            FROM sys_user u
            INNER JOIN sys_user_role ur ON u.id = ur.user_id
            INNER JOIN sys_role r ON ur.role_id = r.id
            LEFT JOIN sys_department d ON u.dept_id = d.id
            WHERE d.id = ? AND r.code = 'TRAINEE'
            """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(TrainingStatisticsDTO.class),
                departmentId
        );
    }

    /**
     * 查询全院培训统计汇总
     */
    public List<TrainingStatisticsDTO> getHospitalStatistics(Long hospitalId) {
        String sql = """
            SELECT
                u.id AS traineeId,
                u.real_name AS traineeName,
                d.dept_name AS departmentName,
                0 AS attendanceCount,
                0 AS totalDays,
                0.00 AS attendanceRate,
                0 AS examCount,
                0.00 AS avgExamScore,
                0.00 AS examPassRate,
                0 AS completedCourseCount,
                0 AS totalCourseCount,
                0.00 AS courseCompletionRate,
                0.00 AS overallScore,
                '待评价' AS rating
            FROM sys_user u
            INNER JOIN sys_user_role ur ON u.id = ur.user_id
            INNER JOIN sys_role r ON ur.role_id = r.id
            LEFT JOIN sys_department d ON u.dept_id = d.id
            WHERE u.hospital_id = ? AND r.code = 'TRAINEE'
            ORDER BY d.dept_name, u.real_name
            """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(TrainingStatisticsDTO.class),
                hospitalId
        );
    }
}
