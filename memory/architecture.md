# 架构设计（2024 信创版）

## 技术栈

### 后端技术栈
- **语言运行时：** Java 21 LTS（支持虚拟线程、性能提升 30%+）
- **核心框架：** Spring Boot 3.3.5（最新稳定版）
- **认证授权：** Sa-Token 1.39.0（替代 Spring Security，轻量高效）
- **ORM 框架：** MyBatis-Flex 1.9.7（替代 JPA，灵活强大）
- **数据库：** openGauss 5.1.0（信创，兼容 PostgreSQL）
- **缓存：** Redis 7.x / 腾讯 Tendis（Session 存储）
- **消息队列：** Apache RocketMQ 5.3.0（国产，高可靠）
- **AI 能力：** Spring AI 1.0.0-M3 + Ollama（可选，本地部署）
- **API 文档：** SpringDoc OpenAPI 3（Swagger 3.0）
- **监控：** Spring Boot Actuator + Prometheus
- **构建工具：** Maven 3.9+
- **密码加密：** BCrypt（spring-security-crypto）

### 前端技术栈
- **核心框架：** Vue 3 + TypeScript + Vite
- **UI 框架：** Vben Admin 5.x (Monorepo) + Ant Design Vue
- **包管理：** pnpm
- **构建：** Vite 5.x

## 多模块 DDD 架构

项目严格遵循领域驱动设计（Domain-Driven Design），后端分为 5 个 Maven 模块：

```
oncoresi-backend/
├── oncoresi-types/         → 共享层：DTO、枚举（无业务逻辑）
├── oncoresi-domain/        → 领域层：实体、仓储接口
├── oncoresi-infra/         → 基础设施层：JPA、数据库、转换器
├── oncoresi-application/   → 应用层：服务编排、事务管理
└── oncoresi-api/           → 接口层：REST、Security、过滤器
```

## 依赖关系图（依赖倒置原则）

```
           oncoresi-api (接口层)
                 ↓
      oncoresi-application (应用层)
            ↙          ↘
   oncoresi-domain   oncoresi-infra
    (领域层) ←────────── (基础设施层)
         ↓              ↓
      oncoresi-types (共享层)
```

**核心原则：**
- 领域层定义接口（Repository Interface）
- 基础设施层实现接口（Repository Implementation）
- 应用层依赖领域接口，不依赖具体实现
- 依赖方向始终向内（从外层到内层）

## 各层职责详解

### 1. types 层（共享类型）
- **职责：** 定义跨层共享的数据结构
- **内容：**
  - DTO：LoginRequest、LoginResponse、Result<T>
  - 枚举：UserRole（8种角色）、SystemModule（7个子系统）
- **原则：**
  - 无业务逻辑
  - 只有数据结构和验证注解
  - 所有层都可以依赖

### 2. domain 层（领域核心）
- **职责：** 定义核心业务模型和规则
- **内容：**
  - 实体（Entity）：User、Role
  - 仓储接口（Repository Interface）：UserRepository
  - 领域服务（Domain Service）：暂无
- **原则：**
  - 框架无关（除验证注解）
  - 包含业务逻辑方法
  - 定义接口，不实现

### 3. infra 层（基础设施）
- **职责：** 实现技术细节和外部集成
- **内容：**
  - 数据库实体（PO）：UserPO、RolePO（MyBatis-Flex 注解）
  - Mapper 接口：UserMapper、RoleMapper（MyBatis-Flex BaseMapper）
  - 仓储实现：UserRepositoryImpl（实现 domain 接口）
  - 转换器：UserConverter（Domain ↔ PO，Spring Bean）
  - 数据库脚本：init.sql（openGauss 兼容）
- **原则：**
  - 实现 domain 定义的接口
  - 包含所有 MyBatis-Flex 注解
  - 负责对象转换

### 4. application 层（应用服务）
- **职责：** 编排业务流程，协调领域和基础设施
- **内容：**
  - 应用服务：AuthService、JwtService
  - 事务管理（@Transactional）
  - DTO 转换
- **原则：**
  - 依赖 domain 接口
  - 不直接访问数据库
  - 处理跨聚合的业务流程

### 5. api 层（接口层）
- **职责：** 暴露 HTTP 接口，处理请求响应
- **内容：**
  - 控制器：AuthController（完整认证接口）
  - 认证配置：SaTokenConfig（Sa-Token 拦截器）
  - 通用配置：CommonConfig（BCrypt 密码加密器）
  - Swagger 注解：@Tag、@Operation（API 文档）
- **原则：**
  - 只处理 HTTP 相关
  - 参数验证（@Valid）
  - 委托给 application 层
  - 使用 Sa-Token 注解鉴权（@SaCheckLogin、@SaCheckRole）

## 前端架构（Vben Monorepo）

```
oncoresi-frontend/
├── apps/
│   └── web-antd/              ← 主应用（医师规培前端）
│       ├── src/
│       │   ├── api/           # API 接口封装
│       │   ├── router/        # 路由配置
│       │   ├── store/         # Pinia 状态管理
│       │   ├── views/         # 页面组件
│       │   └── layouts/       # 布局组件
│       └── vite.config.mts
│
└── packages/                  ← 核心包（共享）
    ├── @core/                 # 框架核心
    ├── locales/               # 国际化
    ├── stores/                # 全局 store
    └── utils/                 # 工具函数
```

## 数据库设计（openGauss）

### 核心表（当前实现）

> **数据库：** openGauss 5.1.0（兼容 PostgreSQL 协议）
> **端口：** 5432（PostgreSQL 标准端口）
> **驱动：** org.opengauss.Driver

1. **sys_user** - 用户表
   - 主键：id (BIGSERIAL，自增)
   - 唯一索引：username
   - 字段：password (BCrypt)、real_name、phone、email、status
   - 时间戳：create_time、update_time（自动填充）

2. **sys_role** - 角色表
   - 主键：id (BIGSERIAL，自增)
   - 唯一索引：code
   - 预置 8 种角色（HOSPITAL_ADMIN、BASE_ADMIN 等）

3. **sys_user_role** - 用户角色关联表
   - 复合主键：(user_id, role_id)
   - 外键级联删除
   - 支持一个用户多个角色

### MyBatis-Flex 配置
- 驼峰命名自动转换（map-underscore-to-camel-case）
- SQL 日志输出（开发环境）
- 自动填充：`onInsertValue = "now()"`、`onUpdateValue = "now()"`

### 未来扩展表（规划中）
- 组织架构：hospital、base、department
- 培训管理：training_plan、rotation_schedule、attendance
- 考试管理：exam、question_bank、exam_paper
- 绩效分析：performance_report、statistics_data

## 配置文件位置

### 后端配置
- **主配置：** `oncoresi-backend/oncoresi-api/src/main/resources/application.yml`
- **数据库：** openGauss（localhost:5432）
- **Redis：** localhost:6379（Sa-Token Session）
- **RocketMQ：** localhost:9876
- **Ollama：** localhost:11434（可选，`spring.ai.enabled=false` 时禁用）

### 前端配置
- **环境变量：** `oncoresi-frontend/apps/web-antd/.env.*`
- **Vite 配置：** `vite.config.mts`

### 数据库脚本
- **初始化脚本：** `oncoresi-backend/oncoresi-infra/src/main/resources/db/init.sql`

### Maven 配置
- **父 POM：** `oncoresi-backend/pom.xml`（定义版本号）

### Docker 配置
- **后端镜像：** `oncoresi-backend/Dockerfile`
- **编排文件：** `docker-compose.yml`（根目录）

## 关键技术决策

1. **DDD 架构** - 清晰的分层和依赖倒置，保持领域纯粹性
2. **Sa-Token 认证** - 轻量级、开箱即用、比 Spring Security 简单
3. **MyBatis-Flex** - 灵活的 SQL 控制、性能优于 JPA
4. **openGauss 数据库** - 信创要求、兼容 PostgreSQL、性能优异
5. **多角色设计** - 一个用户可以拥有多个角色（1:N）
6. **Monorepo 前端** - 模块化、代码共享方便
7. **BCrypt 密码加密** - 行业标准，安全可靠
8. **Docker 部署** - 容器化、一键启动、环境一致性
9. **AI 可选** - Spring AI 可配置开关，不影响核心功能
10. **Swagger 文档** - 自动生成 API 文档，提升开发效率
