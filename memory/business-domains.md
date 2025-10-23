# 业务领域

## 系统概述

OncoResi 是一个**医师规培管理系统**（住院医师规范化培训管理系统），旨在满足国家医师培训要求，支持医院规培全流程的管理、培训、练习、考核、评价。

## 三大限界上下文

根据 DDD 设计，系统划分为三个限界上下文：

### 1. 用户权限上下文（已实现 ✅）
- **用户管理**：用户注册、登录、信息维护
- **角色权限**：8 种角色、权限控制
- **组织架构**：医院、专业基地、科室层级结构（待实现）

### 2. 培训管理上下文（规划中 📋）
- 过程管理系统
- 招录系统
- 能力建设系统

### 3. 考核评价上下文（规划中 📋）
- 技能考试系统
- 理论考试系统
- 绩效分析系统

## 六大业务子系统（已删除病例系统）

### 1. 过程管理系统

**目标：** 管理规培全流程，从计划到考勤再到进度跟踪

**核心功能：**
- **培训计划管理**
  - 制定年度/季度培训计划
  - 设置培训目标和考核标准
  - 计划审批流程

- **轮转安排**
  - 科室轮转排班
  - 轮转周期管理
  - 轮转冲突检测

- **考勤管理**
  - 学员签到签退
  - 请假审批
  - 考勤统计报表

- **培训进度跟踪**
  - 学员培训进度可视化
  - 阶段性目标完成度
  - 预警机制（进度滞后提醒）

**涉及角色：**
- 医院管理员、基地管理员、科室管理员（管理）
- 导师、学员（执行和记录）

**关键实体：**
```
TrainingPlan（培训计划）
├── id, name, description
├── startTime, endTime
├── targetGroup（目标人群）
└── status（计划状态）

RotationSchedule（轮转安排）
├── traineeId, departmentId
├── startDate, endDate
└── supervisor（责任导师）

Attendance（考勤记录）
├── traineeId, date
├── checkInTime, checkOutTime
└── status（正常/请假/迟到）

TrainingProgress（培训进度）
├── traineeId, planId
├── completedTasks
└── progressPercentage
```

---

### 2. 招录系统

**目标：** 管理规培学员招录全流程

**核心功能：**
- **招录公告发布**
  - 发布招录简章
  - 设置报名时间、考试时间
  - 招录条件设置

- **在线报名**
  - 考生信息采集
  - 材料上传（学历证明、证书等）
  - 报名资格预审

- **资格审核**
  - 审核员分配
  - 材料审核流程
  - 审核结果反馈

- **录取管理**
  - 成绩录入
  - 录取名单生成
  - 录取通知发放

**涉及角色：**
- 医院管理员、基地管理员（发布和审核）
- 考生（报名）

**关键实体：**
```
RecruitmentNotice（招录公告）
├── title, content
├── registrationStart, registrationEnd
├── examDate
└── requirementsEnums

Application（报名申请）
├── applicantName, phone, email
├── education, certificates
├── documents（上传材料）
└── status（待审核/通过/拒绝）

QualificationReview（资格审核）
├── applicationId, reviewerId
├── reviewResult, reviewComments
└── reviewTime

Admission（录取信息）
├── applicationId
├── admissionScore
└── admissionStatus
```

---

### 3. 能力建设系统

**目标：** 提供系统化的学习资源和培训任务

**核心功能：**
- **课程管理**
  - 课程创建（视频、文档、PPT）
  - 课程分类（基础/专业/技能）
  - 课程审核发布

- **学习资源库**
  - 教学视频库
  - 教材文档库
  - 参考文献库

- **培训任务分配**
  - 必修任务、选修任务
  - 任务截止时间
  - 任务完成验证

- **学习进度跟踪**
  - 课程学习记录
  - 学习时长统计
  - 学习报告生成

**涉及角色：**
- 导师、教师（发布课程和任务）
- 学员（学习）

**关键实体：**
```
Course（课程）
├── title, description, category
├── instructor, duration
├── videoUrl, documentUrls
└── status（草稿/已发布）

LearningResource（学习资源）
├── name, type（video/doc/pdf）
├── fileUrl, thumbnailUrl
└── tags

TrainingTask（培训任务）
├── taskName, description
├── assignedTo（学员ID）
├── deadline
└── completionStatus

LearningRecord（学习记录）
├── traineeId, courseId
├── studyDuration
├── completionPercentage
└── lastAccessTime
```

---

### 4. ~~临床病例系统~~（已删除）

**已删除原因**: 根据业务调整，病例管理功能已从系统中移除。

---

### ~~4. 临床病例系统~~（已删除，以下内容仅供参考）

**目标：** 收集、管理、讨论临床病例，提升学员临床思维能力

**核心功能：**
- **病例采集**
  - 学员提交病例（患者信息、诊断、治疗方案）
  - 病例格式规范化
  - 病例去标识化（保护隐私）

- **病例讨论**
  - 发起病例讨论
  - 多人在线评论
  - 导师点评和指导

- **病例学习**
  - 典型病例库
  - 病例分类检索
  - 学员学习记录

- **质量评估**
  - 病例质量评分
  - 病例完整性检查
  - 优秀病例评选

**涉及角色：**
- 学员（提交病例）
- 导师、教师（点评和指导）
- 医院管理员（质量监控）

**关键实体：**
```
ClinicalCase（临床病例）
├── caseTitle, patientInfo
├── chiefComplaint（主诉）
├── diagnosis, treatment
├── submittedBy（学员）
└── status（草稿/已提交/已审核）

CaseDiscussion（病例讨论）
├── caseId, initiator
├── discussionTopic
├── comments[]（评论列表）
└── createdAt

CaseStudyRecord（病例学习记录）
├── traineeId, caseId
├── studyNotes
└── studyTime

CaseQualityAssessment（病例质量评估）
├── caseId, assessorId
├── qualityScore
├── completeness（完整性）
└── comments
```

---

### 5. 技能考试系统

**目标：** 管理临床技能操作考核

**核心功能：**
- **考试安排**
  - 创建技能考试（心肺复苏、导尿术等）
  - 设置考试时间、地点
  - 考生分组

- **考场管理**
  - 考场分配
  - 考官安排
  - 考试设备准备

- **技能评估**
  - 评分标准配置
  - 现场打分
  - 视频录制存档

- **成绩管理**
  - 成绩录入
  - 成绩统计分析
  - 不合格学员补考

**涉及角色：**
- 基地管理员、科室管理员（安排考试）
- 导师（担任考官）
- 学员（参加考试）

**关键实体：**
```
SkillExam（技能考试）
├── examName, skillType
├── examDate, location
├── passingScore
└── status

ExamRoom（考场）
├── roomName, capacity
├── equipment（设备列表）
└── assignedExams

SkillAssessment（技能评估）
├── examId, traineeId
├── assessorId（考官）
├── scoringCriteria（评分项）
├── scores（各项得分）
└── videoUrl（考试录像）

ExamScore（考试成绩）
├── traineeId, examId
├── totalScore
├── passed（是否通过）
└── remarks
```

---

### 6. 理论考试系统

**目标：** 在线理论知识考核

**核心功能：**
- **题库管理**
  - 题目录入（单选、多选、判断、简答）
  - 题目分类（按学科、难度）
  - 题目审核

- **组卷管理**
  - 手动组卷
  - 随机组卷
  - 试卷预览

- **在线考试**
  - 考试倒计时
  - 自动交卷
  - 防作弊措施（禁止切屏、人脸识别）

- **成绩分析**
  - 自动阅卷
  - 成绩统计
  - 错题分析

**涉及角色：**
- 导师、教师（出题和组卷）
- 学员（参加考试）

**关键实体：**
```
QuestionBank（题库）
├── questionText, questionType
├── options[], correctAnswer
├── difficulty, subject
└── createdBy

ExamPaper（试卷）
├── paperTitle
├── questions[]（题目列表）
├── totalScore, duration
└── status

TheoryExam（理论考试）
├── paperId, examDate
├── startTime, endTime
├── participants[]（考生列表）
└── status（未开始/进行中/已结束）

ExamAnswer（考试答卷）
├── examId, traineeId
├── answers[]（答案列表）
├── submitTime
├── autoScore（客观题得分）
└── manualScore（主观题得分）
```

---

### 7. 绩效分析系统

**目标：** 统计分析培训数据，生成绩效报告

**核心功能：**
- **数据统计**
  - 学员出勤率统计
  - 考试通过率统计
  - 病例提交数量统计

- **绩效评价**
  - 学员综合评分
  - 导师带教质量评价
  - 科室培训绩效排名

- **报表生成**
  - 月度/季度/年度报表
  - 可视化图表（柱状图、饼图、折线图）
  - 报表导出（PDF、Excel）

- **数据可视化**
  - Dashboard 仪表盘
  - 实时数据大屏
  - 趋势分析

**涉及角色：**
- 医院管理员、基地管理员（查看分析报告）
- 科室管理员（查看科室数据）

**关键实体：**
```
PerformanceReport（绩效报告）
├── reportTitle, reportType
├── reportPeriod（统计周期）
├── targetEntity（学员/科室/基地）
├── metrics[]（指标列表）
└── generatedAt

StatisticsData（统计数据）
├── dataType（出勤/考试/病例）
├── entityId, entityType
├── value, unit
└── statisticsDate

EvaluationMetrics（评价指标）
├── metricName, weight
├── calculationFormula
└── passingThreshold

AnalysisResult（分析结果）
├── analysisType（趋势/对比）
├── chartData（图表数据）
└── conclusion（分析结论）
```

---

## 用户角色矩阵

| 功能模块 | 医院管理员 | 基地管理员 | 科室管理员 | 责任导师 | 带教老师 | 学员 | 护士评价 | 病人评价 |
|---------|----------|----------|----------|---------|---------|-----|---------|---------|
| 过程管理 | ✅ 全部 | ✅ 基地内 | ✅ 科室内 | ✅ 查看 | ✅ 查看 | ✅ 个人 | - | - |
| 招录系统 | ✅ 全部 | ✅ 审核 | - | - | - | - | - | - |
| 能力建设 | ✅ 管理 | ✅ 管理 | ✅ 管理 | ✅ 发布 | ✅ 发布 | ✅ 学习 | - | - |
| 临床病例 | ✅ 查看 | ✅ 查看 | ✅ 查看 | ✅ 点评 | ✅ 点评 | ✅ 提交 | - | - |
| 技能考试 | ✅ 管理 | ✅ 安排 | ✅ 安排 | ✅ 评分 | ✅ 评分 | ✅ 参加 | - | - |
| 理论考试 | ✅ 管理 | ✅ 管理 | ✅ 管理 | ✅ 出题 | ✅ 出题 | ✅ 参加 | - | - |
| 绩效分析 | ✅ 全部 | ✅ 基地内 | ✅ 科室内 | ✅ 个人 | ✅ 个人 | ✅ 个人 | - | - |
| 评价系统 | - | - | - | ✅ 评价 | ✅ 评价 | - | ✅ 评价 | ✅ 评价 |

## 数据关系图

```
┌─────────────┐
│   Hospital  │ (医院)
└──────┬──────┘
       │ 1:N
┌──────▼──────┐
│    Base     │ (专业基地)
└──────┬──────┘
       │ 1:N
┌──────▼──────┐
│ Department  │ (科室)
└──────┬──────┘
       │ 1:N
┌──────▼──────┐       ┌───────────┐
│   Trainee   │◄─────►│   Role    │ (角色)
│   (学员)    │  N:M  └───────────┘
└──────┬──────┘
       │
       ├──► TrainingPlan (培训计划)
       ├──► RotationSchedule (轮转安排)
       ├──► Attendance (考勤)
       ├──► ClinicalCase (临床病例)
       ├──► ExamScore (考试成绩)
       └──► LearningRecord (学习记录)
```

## 开发优先级建议

### 第一阶段 ✅（已完成）
- [x] 用户权限管理
- [x] JWT 认证
- [x] 角色体系

### 第二阶段（建议先做）⭐
1. **组织架构**：医院、基地、科室三级结构
2. **过程管理核心**：培训计划、轮转安排
3. **前端框架**：Vben 集成、路由配置

### 第三阶段
4. **能力建设**：课程管理、学习资源
5. **临床病例**：病例提交、讨论

### 第四阶段
6. **考试系统**：理论考试优先
7. **绩效分析**：基础统计报表

## 业务文档参考

- **详细领域设计：** oncoresi-backend/DOMAIN_DESIGN.md
- **实现总结：** oncoresi-backend/IMPLEMENTATION_SUMMARY.md
- **项目架构：** oncoresi-backend/README.md
