# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 📋 项目简介

**OncoResi** - 医师规培管理系统（住院医师规范化培训管理系统）

一个基于 **DDD 架构**的医疗培训管理平台，支持培训全流程管理：过程管理、招录、能力建设、临床病例、技能/理论考试、绩效分析。

### 技术架构（✨ 2024信创技术栈升级版）
- **后端：** Java 21 LTS + Spring Boot 3.3.5 + Sa-Token 1.39.0
- **ORM：** MyBatis-Flex 1.9.7
- **数据库：** openGauss 5.1.0（信创）+ Redis 7.x
- **消息队列：** RocketMQ 5.3.0
- **AI 能力：** Spring AI + Ollama（本地部署）
- **前端：** Vue 3 + TypeScript + Vite + Vben Admin 5.x
- **架构：** 领域驱动设计（DDD）5 层架构

### 当前实现状态
✅ **第一阶段完成** - Sa-Token 认证 + 8 种角色权限体系
✅ **技术栈升级完成** - 信创版本，全面支持国产化
📋 **第二阶段规划中** - 7 大业务子系统开发

---

## 🚀 快速开始

### 方式一：Docker 一键启动（推荐）
```bash
# 启动所有服务（openGauss、Redis、RocketMQ、Ollama、后端）
docker-compose up -d

# 查看日志
docker-compose logs -f backend
```

### 方式二：本地开发
```bash
# 1. 启动依赖服务
docker-compose up -d opengauss redis ollama

# 2. 启动后端
cd oncoresi-backend/oncoresi-api
mvn spring-boot:run

# 3. 启动前端
cd oncoresi-frontend
pnpm install
pnpm dev:antd
```

### 测试账号
```
管理员：admin / admin123    (HOSPITAL_ADMIN)
教师：  teacher / admin123  (SUPERVISOR + TEACHER)
学员：  student / admin123  (TRAINEE)
```

### 访问地址
- **前端：** http://localhost:5173
- **后端 API：** http://localhost:8080/api
- **Swagger UI：** http://localhost:8080/api/swagger-ui.html
- **健康检查：** http://localhost:8080/api/actuator/health

---

## 📚 系统核心特性

### 八种用户角色
医院管理员 | 基地管理员 | 科室管理员 | 责任导师 | 带教老师 | 学员 | 护士评价 | 病人评价

### 七大业务子系统（规划）
1. **过程管理** - 培训计划、轮转安排、考勤、进度跟踪
2. **招录系统** - 招录公告、在线报名、资格审核、录取管理
3. **能力建设** - 课程管理、学习资源、培训任务、学习记录
4. **临床病例** - 病例采集、病例讨论、病例学习、质量评估
5. **技能考试** - 考试安排、考场管理、技能评估、成绩管理
6. **理论考试** - 题库管理、在线考试、自动阅卷、成绩分析
7. **绩效分析** - 数据统计、绩效评价、报表生成、可视化

### 三大限界上下文（DDD）
- **用户权限上下文** ✅ 已实现
- **培训管理上下文** 📋 规划中
- **考核评价上下文** 📋 规划中

---

## 🗂️ 项目结构

### 后端模块（DDD 五层架构）
```
oncoresi-backend/
├── oncoresi-types/         → 共享类型（DTO、枚举）
├── oncoresi-domain/        → 领域模型（实体、仓储接口）
├── oncoresi-infra/         → 基础设施（JPA、数据库）
├── oncoresi-application/   → 应用服务（业务编排）
└── oncoresi-api/           → 接口层（REST、Security）
```

**依赖方向：** api → application → domain ← infra
**核心原则：** 领域层定义接口，基础设施层实现接口（依赖倒置）

### 前端结构（Vben Monorepo）
```
oncoresi-frontend/
├── apps/web-antd/          → 主应用（医师规培前端）
└── packages/               → 核心包（组件、工具、状态）
```

---

## 📖 详细文档索引

所有详细文档位于 `/memory` 目录：

### 🏗️ [架构设计](memory/architecture.md)
- 技术栈详解
- DDD 五层架构
- 各层职责和原则
- 数据库设计
- 前后端架构

### ✨ [技术栈升级总结](memory/tech-stack-upgrade-2024.md)
- 信创技术栈升级详情
- Java 21 + Spring Boot 3.3.5
- Sa-Token + MyBatis-Flex + openGauss
- Spring AI + Ollama 本地 AI
- Docker 部署完整方案

### ⚙️ [构建和运行命令](memory/commands.md)
- Maven 构建命令
- 应用启动方式
- 数据库初始化
- API 测试命令
- Git 操作
- Docker 命令

### 💻 [开发指南](memory/development-guide.md)
- DDD 五层开发流程（完整示例）
- 开发原则和规范
- 前端 API 调用封装
- 常见问题解决
- 代码检查清单

### 🔐 [安全机制](memory/security.md)
- JWT 认证流程图
- 八种角色权限详解
- Spring Security 配置
- BCrypt 密码加密
- 安全最佳实践

### 🏥 [业务领域](memory/business-domains.md)
- 七大子系统详细设计
- 核心实体和关系
- 用户角色矩阵
- 数据关系图
- 开发优先级建议

### 🚀 [部署指南](memory/deployment.md)
- 生产环境检查清单
- Docker 部署（docker-compose）
- 传统部署（Systemd）
- SSL/TLS 配置
- 监控和日志
- 性能优化
- 备份和恢复

---

## 🔑 核心文件位置

### 后端关键文件
- **入口：** `oncoresi-api/src/main/java/com/oncoresi/OncoresiApplication.java`
- **安全配置：** `oncoresi-api/src/main/java/com/oncoresi/api/config/SecurityConfig.java`
- **JWT 过滤器：** `oncoresi-api/src/main/java/com/oncoresi/api/security/JwtAuthenticationFilter.java`
- **认证服务：** `oncoresi-application/src/main/java/com/oncoresi/application/service/AuthService.java`
- **JWT 服务：** `oncoresi-application/src/main/java/com/oncoresi/application/service/JwtService.java`
- **用户实体：** `oncoresi-domain/src/main/java/com/oncoresi/domain/entity/User.java`
- **数据库脚本：** `oncoresi-infra/src/main/resources/db/init.sql`
- **配置文件：** `oncoresi-api/src/main/resources/application.yml`

### 前端关键文件
- **主应用配置：** `oncoresi-frontend/apps/web-antd/vite.config.mts`
- **路由配置：** `oncoresi-frontend/apps/web-antd/src/router/`
- **API 封装：** `oncoresi-frontend/apps/web-antd/src/api/`
- **前端精简指南：** `oncoresi-frontend/CLEANUP_GUIDE.md`

### 项目文档
- **架构总览：** `oncoresi-backend/README.md`
- **领域设计：** `oncoresi-backend/DOMAIN_DESIGN.md`
- **实现总结：** `oncoresi-backend/IMPLEMENTATION_SUMMARY.md`
- **API 测试指南：** `oncoresi-backend/LOGIN_TEST.md`

---

## 💡 开发提示

### DDD 开发五步法
1. **Domain 层** - 定义实体和仓储接口
2. **Types 层** - 创建 DTO（Request/Response）
3. **Infra 层** - 实现 JPA 持久化和仓储
4. **Application 层** - 编写业务服务
5. **API 层** - 创建 REST 控制器

### 核心开发原则
- ✅ 保持 DDD 分层，API 层不直接访问 Infra 层
- ✅ 领域层纯粹，不依赖框架（除验证注解）
- ✅ 依赖倒置，Domain 定义接口，Infra 实现
- ✅ 一个事务操作一个聚合根
- ✅ DTO 转换在 Application 层，不在 Controller
- ✅ 所有数据库访问通过 Repository 接口

### 常见问题速查
- **构建失败** → 检查 pom.xml 模块依赖
- **JPA vs Domain** → UserPO (infra) vs User (domain)，用 Converter 转换
- **JWT 密钥** → 生产环境改为环境变量
- **数据库更新** → 开发用 `update`，生产用 `validate`
- **CORS 问题** → 在 SecurityConfig 配置 CorsFilter

---

## 🎯 下一步开发建议

### 第二阶段（建议优先）
1. **组织架构模块** - 医院/基地/科室三级结构
2. **过程管理核心** - 培训计划、轮转安排
3. **前端集成** - Vben 路由配置、API 对接

### 技术债务
- [ ] 添加单元测试和集成测试
- [ ] 接入 Redis 缓存
- [ ] 实现 Token 刷新机制
- [ ] 添加 Swagger API 文档
- [ ] 配置 CORS 策略
- [ ] 实现全局异常处理
- [ ] 添加限流保护

---

## 📞 相关资源

- **项目 Git：** master 分支（主分支）
- **最新提交：** 92a17ba - 基础框架
- **开发环境：** Windows / Java 8+ / MySQL 8.0+ / Node 20+

---

**📌 提示：** 当需要详细实现指导时，请查阅 `/memory` 目录下的对应文档。本文件仅作为快速索引和概览。