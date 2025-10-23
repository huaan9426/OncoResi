package com.oncoresi.domain.aggregate;

import com.oncoresi.domain.event.CourseCompletedEvent;
import com.oncoresi.domain.exception.DomainException;
import com.oncoresi.domain.valueobject.CourseCategory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 课程聚合根
 * 封装课程、学习资源、学习记录等业务逻辑
 */
@Getter
public class CourseAggregate extends AggregateRoot<Long> {

    /**
     * 课程ID
     */
    private Long id;

    /**
     * 课程标题
     */
    private String title;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 课程类别
     */
    private CourseCategory category;

    /**
     * 讲师ID
     */
    private Long instructorId;

    /**
     * 课程时长（分钟）
     */
    private int duration;

    /**
     * 课程状态
     */
    private CourseStatus status;

    /**
     * 学习资源列表
     */
    private final List<LearningResource> resources;

    /**
     * 学习记录列表
     */
    private final Map<Long, LearningRecord> records; // traineeId -> LearningRecord

    /**
     * 课程状态枚举
     */
    public enum CourseStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    /**
     * 学习资源（聚合内实体）
     */
    @Getter
    public static class LearningResource {
        private Long id;
        private String name;
        private String type; // video, doc, pdf, etc.
        private String fileUrl;
        private int durationMinutes;

        public LearningResource(Long id, String name, String type, String fileUrl, int durationMinutes) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.fileUrl = fileUrl;
            this.durationMinutes = durationMinutes;
        }
    }

    /**
     * 学习记录（聚合内实体）
     */
    @Getter
    public static class LearningRecord {
        private Long traineeId;
        private int studyMinutes;
        private int completionPercentage;
        private LocalDateTime lastAccessTime;
        private boolean completed;

        public LearningRecord(Long traineeId) {
            this.traineeId = traineeId;
            this.studyMinutes = 0;
            this.completionPercentage = 0;
            this.completed = false;
        }

        public void addStudyTime(int minutes) {
            this.studyMinutes += minutes;
            this.lastAccessTime = LocalDateTime.now();
        }

        public void updateProgress(int percentage) {
            this.completionPercentage = Math.min(100, percentage);
            if (this.completionPercentage >= 100) {
                this.completed = true;
            }
        }

        public void complete() {
            this.completed = true;
            this.completionPercentage = 100;
        }
    }

    /**
     * 私有构造函数
     */
    private CourseAggregate(Long id, String title, String description, CourseCategory category,
                            Long instructorId, int duration, CourseStatus status) {
        this.id = id;
        this.title = Objects.requireNonNull(title, "课程标题不能为空");
        this.description = description;
        this.category = Objects.requireNonNull(category, "课程类别不能为空");
        this.instructorId = instructorId;
        this.duration = duration;
        this.status = status != null ? status : CourseStatus.DRAFT;
        this.resources = new ArrayList<>();
        this.records = new HashMap<>();

        if (duration < 0) {
            throw DomainException.of("课程时长不能为负数");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * 重建聚合
     */
    public static CourseAggregate reconstitute(Long id, String title, String description, CourseCategory category,
                                                Long instructorId, int duration, CourseStatus status,
                                                List<LearningResource> resources) {
        CourseAggregate aggregate = new CourseAggregate(id, title, description, category, instructorId, duration, status);
        if (resources != null) {
            aggregate.resources.addAll(resources);
        }
        return aggregate;
    }

    /**
     * 创建新课程
     */
    public static CourseAggregate create(String title, String description, CourseCategory category,
                                          Long instructorId, int duration) {
        return new CourseAggregate(null, title, description, category, instructorId, duration, CourseStatus.DRAFT);
    }

    /**
     * 添加学习资源
     */
    public void addResource(LearningResource resource) {
        if (status == CourseStatus.ARCHIVED) {
            throw DomainException.of("已归档的课程无法添加资源");
        }

        resources.add(resource);
    }

    /**
     * 发布课程
     */
    public void publish() {
        if (status != CourseStatus.DRAFT) {
            throw DomainException.of("只有草稿状态的课程可以发布");
        }

        if (resources.isEmpty()) {
            throw DomainException.of("课程至少需要一个学习资源");
        }

        this.status = CourseStatus.PUBLISHED;
    }

    /**
     * 归档课程
     */
    public void archive() {
        if (status == CourseStatus.ARCHIVED) {
            throw DomainException.of("课程已归档");
        }

        this.status = CourseStatus.ARCHIVED;
    }

    /**
     * 记录学习
     */
    public void recordStudy(Long traineeId, int studyMinutes) {
        if (!isPublished()) {
            throw DomainException.of("课程未发布，无法学习");
        }

        LearningRecord record = records.computeIfAbsent(traineeId, LearningRecord::new);
        record.addStudyTime(studyMinutes);

        // 计算进度
        int percentage = Math.min(100, (record.getStudyMinutes() * 100) / duration);
        record.updateProgress(percentage);

        // 如果完成，发布事件
        if (record.isCompleted()) {
            addDomainEvent(new CourseCompletedEvent(this.id, traineeId));
        }
    }

    /**
     * 判断课程是否已发布
     */
    public boolean isPublished() {
        return status == CourseStatus.PUBLISHED;
    }

    /**
     * 获取完成人数
     */
    public long getCompletedCount() {
        return records.values().stream()
                .filter(LearningRecord::isCompleted)
                .count();
    }

    /**
     * 获取学习人数
     */
    public int getStudentCount() {
        return records.size();
    }

    /**
     * 设置ID（用于持久化后）
     */
    public void setId(Long id) {
        this.id = id;
    }
}
