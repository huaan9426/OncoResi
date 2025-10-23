package com.oncoresi.domain.aggregate;

import com.oncoresi.domain.event.PhaseCompletedEvent;
import com.oncoresi.domain.event.RotationAddedEvent;
import com.oncoresi.domain.event.TrainingPlanCreatedEvent;
import com.oncoresi.domain.exception.DomainException;
import com.oncoresi.domain.valueobject.RotationPeriod;
import com.oncoresi.domain.valueobject.TrainingPhase;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 培训计划聚合根
 * 封装培训计划、阶段、轮转、考勤等业务逻辑
 */
@Getter
public class TrainingPlanAggregate extends AggregateRoot<Long> {

    /**
     * 培训计划ID
     */
    private Long id;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 计划描述
     */
    private String description;

    /**
     * 计划状态（DRAFT-草稿, ACTIVE-进行中, COMPLETED-已完成, CANCELLED-已取消）
     */
    private PlanStatus status;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 培训阶段列表
     */
    private final List<TrainingPhase> phases;

    /**
     * 轮转安排列表（简化，实际应该是实体）
     */
    private final List<RotationSchedule> rotations;

    /**
     * 计划状态枚举
     */
    public enum PlanStatus {
        DRAFT, ACTIVE, COMPLETED, CANCELLED
    }

    /**
     * 轮转安排（聚合内实体）
     */
    @Getter
    public static class RotationSchedule {
        private Long id;
        private Long traineeId;
        private Long departmentId;
        private RotationPeriod period;
        private Long supervisorId;

        public RotationSchedule(Long id, Long traineeId, Long departmentId, RotationPeriod period, Long supervisorId) {
            this.id = id;
            this.traineeId = traineeId;
            this.departmentId = departmentId;
            this.period = period;
            this.supervisorId = supervisorId;
        }
    }

    /**
     * 私有构造函数
     */
    private TrainingPlanAggregate(Long id, String planName, String description, PlanStatus status,
                                   LocalDate startDate, LocalDate endDate,
                                   List<TrainingPhase> phases, List<RotationSchedule> rotations) {
        this.id = id;
        this.planName = Objects.requireNonNull(planName, "计划名称不能为空");
        this.description = description;
        this.status = status != null ? status : PlanStatus.DRAFT;
        this.startDate = Objects.requireNonNull(startDate, "开始日期不能为空");
        this.endDate = Objects.requireNonNull(endDate, "结束日期不能为空");
        this.phases = new ArrayList<>(phases != null ? phases : List.of());
        this.rotations = new ArrayList<>(rotations != null ? rotations : List.of());

        if (endDate.isBefore(startDate)) {
            throw DomainException.of("结束日期不能早于开始日期");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * 重建聚合
     */
    public static TrainingPlanAggregate reconstitute(Long id, String planName, String description,
                                                      PlanStatus status, LocalDate startDate, LocalDate endDate,
                                                      List<TrainingPhase> phases, List<RotationSchedule> rotations) {
        return new TrainingPlanAggregate(id, planName, description, status, startDate, endDate, phases, rotations);
    }

    /**
     * 创建新培训计划
     */
    public static TrainingPlanAggregate create(String planName, String description,
                                                LocalDate startDate, LocalDate endDate) {
        TrainingPlanAggregate aggregate = new TrainingPlanAggregate(
                null, planName, description, PlanStatus.DRAFT,
                startDate, endDate, List.of(), List.of()
        );

        aggregate.addDomainEvent(new TrainingPlanCreatedEvent(aggregate.id, planName));

        return aggregate;
    }

    /**
     * 添加培训阶段
     */
    public void addPhase(TrainingPhase phase) {
        if (status == PlanStatus.COMPLETED || status == PlanStatus.CANCELLED) {
            throw DomainException.of("已完成或已取消的计划无法添加阶段");
        }

        // 验证阶段时间在计划时间范围内
        if (phase.startDate().isBefore(startDate) || phase.endDate().isAfter(endDate)) {
            throw DomainException.of("阶段时间必须在计划时间范围内");
        }

        phases.add(phase);
    }

    /**
     * 添加轮转安排
     */
    public void addRotation(RotationSchedule rotation) {
        if (!isActive()) {
            throw DomainException.of("只能向进行中的计划添加轮转");
        }

        // 检查时间冲突
        for (RotationSchedule existing : rotations) {
            if (existing.getTraineeId().equals(rotation.getTraineeId()) &&
                existing.getPeriod().overlaps(rotation.getPeriod())) {
                throw DomainException.of("学员轮转时间冲突");
            }
        }

        rotations.add(rotation);

        addDomainEvent(new RotationAddedEvent(this.id, rotation.getId()));
    }

    /**
     * 完成阶段
     */
    public void completePhase(String phaseName) {
        TrainingPhase phase = phases.stream()
                .filter(p -> p.phaseName().equals(phaseName))
                .findFirst()
                .orElseThrow(() -> DomainException.of("阶段不存在: " + phaseName));

        if (!phase.isOverdue()) {
            throw DomainException.of("阶段尚未结束");
        }

        addDomainEvent(new PhaseCompletedEvent(this.id, phaseName));
    }

    /**
     * 激活计划
     */
    public void activate() {
        if (status != PlanStatus.DRAFT) {
            throw DomainException.of("只有草稿状态的计划可以激活");
        }

        if (phases.isEmpty()) {
            throw DomainException.of("计划至少需要一个培训阶段");
        }

        this.status = PlanStatus.ACTIVE;
    }

    /**
     * 完成计划
     */
    public void complete() {
        if (status != PlanStatus.ACTIVE) {
            throw DomainException.of("只有进行中的计划可以完成");
        }

        this.status = PlanStatus.COMPLETED;
    }

    /**
     * 取消计划
     */
    public void cancel() {
        if (status == PlanStatus.COMPLETED) {
            throw DomainException.of("已完成的计划无法取消");
        }

        this.status = PlanStatus.CANCELLED;
    }

    /**
     * 判断计划是否进行中
     */
    public boolean isActive() {
        return status == PlanStatus.ACTIVE;
    }

    /**
     * 设置ID（用于持久化后）
     */
    public void setId(Long id) {
        this.id = id;
    }
}
