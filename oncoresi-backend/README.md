# 医师规培管理系统 (Oncoresi Backend)

## 项目简介
基于DDD(领域驱动设计)架构的医师规范化培训管理系统,满足医院规培过程中管理、培训、练习、考核、评价等全方位需求。

## 技术栈
- Java 1.8
- Spring Boot 2.6.13
- Spring Data JPA
- Spring Security
- MySQL 8.0
- Redis
- Maven 多模块

## 项目结构 (DDD架构)

```
oncoresi-backend/
├── oncoresi-api/              # 接口层
│   ├── controller/           # REST Controllers
│   ├── config/              # 配置类
│   ├── security/            # 安全认证
│   └── interceptor/         # 拦截器
│
├── oncoresi-application/      # 应用层
│   ├── service/             # 应用服务(服务编排)
│   ├── command/             # 命令对象
│   ├── query/               # 查询对象
│   └── assembler/           # 对象转换器
│
├── oncoresi-domain/           # 领域层(核心业务逻辑)
│   ├── entity/              # 实体
│   ├── valueobject/         # 值对象
│   ├── service/             # 领域服务
│   ├── repository/          # 仓储接口
│   └── event/               # 领域事件
│
├── oncoresi-infra/            # 基础设施层
│   ├── persistence/         # 数据库实现
│   ├── config/              # 基础配置
│   └── external/            # 外部服务
│
└── oncoresi-types/            # 共享类型
    ├── dto/                 # 数据传输对象
    ├── enums/               # 枚举类
    └── constants/           # 常量定义
```

## 系统模块 (7大子系统)

1. **过程管理系统** - 规培过程跟踪与管理
2. **招录系统** - 学员招募与录取
3. **能力建设系统** - 培训能力提升
4. **临床病例系统** - 病例管理与学习
5. **技能考试系统** - 技能考核评估
6. **理论考试系统** - 理论知识考试
7. **绩效分析系统** - 培训绩效分析

## 角色权限 (8种角色)

1. **医院管理员** (HOSPITAL_ADMIN) - 全局管理权限
2. **专业基地管理员** (BASE_ADMIN) - 专业基地管理
3. **科室管理员** (DEPT_ADMIN) - 科室级管理
4. **责任导师** (SUPERVISOR) - 学员指导与评价
5. **带教老师** (TEACHER) - 日常带教
6. **学员** (TRAINEE) - 参与培训与考核
7. **护士评价** (NURSE_EVALUATOR) - 护士维度评价
8. **病人评价** (PATIENT_EVALUATOR) - 患者维度评价

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 编译项目
```bash
mvn clean install
```

### 运行项目
```bash
cd oncoresi-api
mvn spring-boot:run
```

### 访问地址
- 接口地址: http://localhost:8080/api
- API文档: http://localhost:8080/api/swagger-ui.html (待配置)

## 开发规范

### DDD分层职责

1. **oncoresi-api (接口层)**
   - 处理HTTP请求/响应
   - 参数验证与异常处理
   - 不包含业务逻辑

2. **oncoresi-application (应用层)**
   - 服务编排,调用领域服务
   - 事务控制
   - DTO与领域对象转换

3. **oncoresi-domain (领域层)**
   - 核心业务逻辑
   - 领域模型设计
   - 不依赖外部技术框架

4. **oncoresi-infra (基础设施层)**
   - 实现领域层定义的仓储接口
   - 数据库访问
   - 第三方服务集成

5. **oncoresi-types (共享类型)**
   - 跨模块共享的类型定义
   - 无业务逻辑

### 依赖规则
```
api -> application -> domain <- infra
             ↓
          types
```

## 数据库设计
(待补充)

## API文档
(待补充)

## License
Copyright © 2025 Oncoresi
