package com.oncoresi.domain.aggregate;

import com.oncoresi.domain.event.ApplicationReviewedEvent;
import com.oncoresi.domain.exception.DomainException;
import com.oncoresi.domain.valueobject.RecruitmentStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 招录聚合根
 * 封装招录公告、报名、审核、录取等业务逻辑
 */
@Getter
public class RecruitmentAggregate extends AggregateRoot<Long> {

    /**
     * 招录公告ID
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 报名开始日期
     */
    private LocalDate registrationStart;

    /**
     * 报名截止日期
     */
    private LocalDate registrationEnd;

    /**
     * 考试日期
     */
    private LocalDate examDate;

    /**
     * 招录人数
     */
    private int recruitmentCount;

    /**
     * 报名申请列表
     */
    private final List<Application> applications;

    /**
     * 报名申请（聚合内实体）
     */
    @Getter
    public static class Application {
        private Long id;
        private String applicantName;
        private String phone;
        private String email;
        private String education;
        private RecruitmentStatus status;
        private String reviewComments;
        private Long reviewerId;
        private LocalDateTime reviewTime;
        private LocalDateTime submitTime;

        public Application(Long id, String applicantName, String phone, String email, String education) {
            this.id = id;
            this.applicantName = applicantName;
            this.phone = phone;
            this.email = email;
            this.education = education;
            this.status = RecruitmentStatus.REGISTERING;
            this.submitTime = LocalDateTime.now();
        }

        public void approve(Long reviewerId, String comments) {
            if (!status.needsReview()) {
                throw DomainException.of("当前状态无法审核");
            }
            this.status = RecruitmentStatus.REVIEW_PASSED;
            this.reviewerId = reviewerId;
            this.reviewComments = comments;
            this.reviewTime = LocalDateTime.now();
        }

        public void reject(Long reviewerId, String comments) {
            if (!status.needsReview()) {
                throw DomainException.of("当前状态无法审核");
            }
            this.status = RecruitmentStatus.REVIEW_REJECTED;
            this.reviewerId = reviewerId;
            this.reviewComments = comments;
            this.reviewTime = LocalDateTime.now();
        }

        public void admit() {
            if (status != RecruitmentStatus.REVIEW_PASSED) {
                throw DomainException.of("只有审核通过的申请可以录取");
            }
            this.status = RecruitmentStatus.ADMITTED;
        }

        public void updateStatus(RecruitmentStatus newStatus) {
            this.status = newStatus;
        }
    }

    /**
     * 私有构造函数
     */
    private RecruitmentAggregate(Long id, String title, String content,
                                  LocalDate registrationStart, LocalDate registrationEnd,
                                  LocalDate examDate, int recruitmentCount) {
        this.id = id;
        this.title = Objects.requireNonNull(title, "标题不能为空");
        this.content = content;
        this.registrationStart = Objects.requireNonNull(registrationStart, "报名开始日期不能为空");
        this.registrationEnd = Objects.requireNonNull(registrationEnd, "报名截止日期不能为空");
        this.examDate = examDate;
        this.recruitmentCount = recruitmentCount;
        this.applications = new ArrayList<>();

        if (registrationEnd.isBefore(registrationStart)) {
            throw DomainException.of("报名截止日期不能早于开始日期");
        }

        if (recruitmentCount <= 0) {
            throw DomainException.of("招录人数必须大于0");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * 重建聚合
     */
    public static RecruitmentAggregate reconstitute(Long id, String title, String content,
                                                     LocalDate registrationStart, LocalDate registrationEnd,
                                                     LocalDate examDate, int recruitmentCount,
                                                     List<Application> applications) {
        RecruitmentAggregate aggregate = new RecruitmentAggregate(
                id, title, content, registrationStart, registrationEnd, examDate, recruitmentCount
        );
        if (applications != null) {
            aggregate.applications.addAll(applications);
        }
        return aggregate;
    }

    /**
     * 创建招录公告
     */
    public static RecruitmentAggregate create(String title, String content,
                                               LocalDate registrationStart, LocalDate registrationEnd,
                                               LocalDate examDate, int recruitmentCount) {
        return new RecruitmentAggregate(null, title, content, registrationStart, registrationEnd, examDate, recruitmentCount);
    }

    /**
     * 提交报名申请
     */
    public Long submitApplication(String applicantName, String phone, String email, String education) {
        if (!isInRegistrationPeriod()) {
            throw DomainException.of("当前不在报名期内");
        }

        if (applications.size() >= recruitmentCount * 10) { // 假设最多接受10倍报名
            throw DomainException.of("报名人数已满");
        }

        Long applicationId = (long) (applications.size() + 1); // 简化ID生成
        Application application = new Application(applicationId, applicantName, phone, email, education);
        applications.add(application);

        return applicationId;
    }

    /**
     * 审核报名申请
     */
    public void reviewApplication(Long applicationId, Long reviewerId, boolean approved, String comments) {
        Application application = findApplication(applicationId);

        if (approved) {
            application.approve(reviewerId, comments);
        } else {
            application.reject(reviewerId, comments);
        }

        addDomainEvent(new ApplicationReviewedEvent(applicationId, approved));
    }

    /**
     * 批量录取
     */
    public void admitApplications(List<Long> applicationIds) {
        if (applicationIds.size() > recruitmentCount) {
            throw DomainException.of("录取人数不能超过招录人数");
        }

        for (Long appId : applicationIds) {
            Application application = findApplication(appId);
            application.admit();
        }
    }

    /**
     * 判断是否在报名期内
     */
    public boolean isInRegistrationPeriod() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(registrationStart) && !now.isAfter(registrationEnd);
    }

    /**
     * 获取审核通过的申请数量
     */
    public long getApprovedCount() {
        return applications.stream()
                .filter(app -> app.getStatus() == RecruitmentStatus.REVIEW_PASSED)
                .count();
    }

    /**
     * 获取已录取的申请数量
     */
    public long getAdmittedCount() {
        return applications.stream()
                .filter(app -> app.getStatus() == RecruitmentStatus.ADMITTED)
                .count();
    }

    /**
     * 查找申请
     */
    private Application findApplication(Long applicationId) {
        return applications.stream()
                .filter(app -> app.getId().equals(applicationId))
                .findFirst()
                .orElseThrow(() -> DomainException.of("申请不存在: " + applicationId));
    }

    /**
     * 设置ID（用于持久化后）
     */
    public void setId(Long id) {
        this.id = id;
    }
}
