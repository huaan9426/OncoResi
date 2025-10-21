# 领域模型设计文档

## 业务需求分析

### 核心业务场景
系统需按照国家医师培训要求,满足医院规培过程中管理、培训、练习、考核、评价等全方面的需求。

### 子系统分析

#### 1. 过程管理系统 (ProcessManagement)
**核心功能:**
- 培训计划制定与管理
- 培训进度跟踪
- 轮转科室管理
- 考勤管理

**领域对象:**
- `TrainingPlan` - 培训计划(聚合根)
- `RotationSchedule` - 轮转计划
- `Attendance` - 考勤记录
- `TrainingProgress` - 培训进度

---

#### 2. 招录系统 (Recruitment)
**核心功能:**
- 招生简章发布
- 学员报名
- 资格审核
- 录取管理

**领域对象:**
- `RecruitmentNotice` - 招生简章(聚合根)
- `Application` - 申请表
- `QualificationReview` - 资格审核
- `Admission` - 录取记录

---

#### 3. 能力建设系统 (CapacityBuilding)
**核心功能:**
- 培训课程管理
- 学习资源管理
- 培训任务分配
- 学习进度跟踪

**领域对象:**
- `Course` - 课程(聚合根)
- `LearningResource` - 学习资源
- `TrainingTask` - 培训任务
- `LearningRecord` - 学习记录

---

#### 4. 临床病例系统 (ClinicalCase)
**核心功能:**
- 病例收集与管理
- 病例讨论
- 病例学习
- 病例质量评估

**领域对象:**
- `ClinicalCase` - 临床病例(聚合根)
- `CaseDiscussion` - 病例讨论
- `CaseStudyRecord` - 病例学习记录
- `CaseQualityAssessment` - 病例质量评估

---

#### 5. 技能考试系统 (SkillExam)
**核心功能:**
- 考试计划制定
- 考场安排
- 技能考核
- 成绩管理

**领域对象:**
- `SkillExam` - 技能考试(聚合根)
- `ExamRoom` - 考场
- `SkillAssessment` - 技能评估
- `ExamScore` - 考试成绩

---

#### 6. 理论考试系统 (TheoryExam)
**核心功能:**
- 题库管理
- 组卷管理
- 在线考试
- 成绩分析

**领域对象:**
- `QuestionBank` - 题库(聚合根)
- `ExamPaper` - 试卷
- `TheoryExam` - 理论考试
- `ExamAnswer` - 考试答案

---

#### 7. 绩效分析系统 (PerformanceAnalysis)
**核心功能:**
- 培训数据统计
- 绩效评估
- 报表生成
- 数据可视化

**领域对象:**
- `PerformanceReport` - 绩效报告(聚合根)
- `StatisticsData` - 统计数据
- `EvaluationMetrics` - 评估指标
- `AnalysisResult` - 分析结果

---

## 通用领域 (Shared Kernel)

### 用户与权限管理
**领域对象:**
- `User` - 用户(聚合根)
- `Role` - 角色
- `Permission` - 权限
- `Organization` - 组织架构(医院/基地/科室)

**角色定义:**
```
1. HOSPITAL_ADMIN - 医院管理员
2. BASE_ADMIN - 专业基地管理员
3. DEPT_ADMIN - 科室管理员
4. SUPERVISOR - 责任导师
5. TEACHER - 带教老师
6. TRAINEE - 学员
7. NURSE_EVALUATOR - 护士评价
8. PATIENT_EVALUATOR - 病人评价
```

### 评价系统
**领域对象:**
- `Evaluation` - 评价(聚合根)
- `EvaluationCriteria` - 评价标准
- `EvaluationResult` - 评价结果
- `Evaluator` - 评价人

**评价类型:**
- 导师评价
- 同行评价
- 护士评价
- 患者评价
- 自我评价

---

## 限界上下文划分

```
┌─────────────────────────────────────────────────────────┐
│                    医师规培管理系统                        │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ 用户权限上下文  │  │ 培训管理上下文  │  │ 考核评价上下文  │  │
│  │              │  │              │  │              │  │
│  │ - 用户管理    │  │ - 过程管理    │  │ - 技能考试    │  │
│  │ - 角色权限    │  │ - 招录管理    │  │ - 理论考试    │  │
│  │ - 组织架构    │  │ - 能力建设    │  │ - 评价系统    │  │
│  │              │  │ - 病例管理    │  │ - 绩效分析    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## 聚合根识别

### 主要聚合根
1. **User** - 用户
2. **Organization** - 组织
3. **TrainingPlan** - 培训计划
4. **Trainee** - 学员
5. **Course** - 课程
6. **ClinicalCase** - 临床病例
7. **Exam** - 考试(包含技能和理论)
8. **Evaluation** - 评价

## 领域事件设计

### 关键领域事件
1. `TraineeRegisteredEvent` - 学员注册事件
2. `TrainingPlanCreatedEvent` - 培训计划创建事件
3. `RotationCompletedEvent` - 轮转完成事件
4. `ExamCompletedEvent` - 考试完成事件
5. `EvaluationSubmittedEvent` - 评价提交事件
6. `TrainingPhaseChangedEvent` - 培训阶段变更事件

## 仓储接口设计

### 主要仓储
- `UserRepository`
- `OrganizationRepository`
- `TrainingPlanRepository`
- `TraineeRepository`
- `CourseRepository`
- `ClinicalCaseRepository`
- `ExamRepository`
- `EvaluationRepository`

## 领域服务设计

### 核心领域服务
1. `TrainingScheduleService` - 培训计划编排服务
2. `ExamScoringService` - 考试评分服务
3. `PerformanceCalculationService` - 绩效计算服务
4. `QualificationVerificationService` - 资格验证服务

## 值对象设计

### 通用值对象
- `PersonalInfo` - 个人信息
- `ContactInfo` - 联系方式
- `Address` - 地址
- `DateRange` - 日期范围
- `Score` - 分数
- `Grade` - 等级

## 下一步工作

1. 细化每个限界上下文的领域模型
2. 设计数据库表结构
3. 实现核心聚合根
4. 定义仓储接口
5. 实现领域服务
6. 配置应用服务层
7. 实现REST API

---

*本文档持续更新中...*
