# 构建和运行命令（2024 信创版）

## 环境要求

- **Java：** 21 LTS
- **Maven：** 3.9+
- **Node.js：** 20+
- **pnpm：** 9+
- **Docker：** 20.10+（可选）

## 后端开发命令

### Maven 构建

```bash
# 构建所有模块（首次构建或依赖变更后）
mvn clean install

# 跳过测试快速构建
mvn clean install -DskipTests

# 构建特定模块
mvn clean install -pl oncoresi-api

# 打包（不安装到本地仓库）
mvn clean package
```

### 启动应用

#### 方式一：Docker 一键启动（推荐）

```bash
# 启动所有服务（openGauss、Redis、RocketMQ、后端）
docker-compose up -d

# 查看后端日志
docker-compose logs -f backend

# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

#### 方式二：本地开发（需先启动依赖服务）

```bash
# 1. 启动依赖服务（openGauss、Redis）
docker-compose up -d opengauss redis

# 2. 启动后端
cd oncoresi-backend/oncoresi-api
mvn spring-boot:run

# 3. 或者运行打包后的 JAR
mvn clean package
java -jar oncoresi-api/target/oncoresi-api-0.0.1-SNAPSHOT.jar

# 4. 或在 IDE 中运行 OncoresiApplication.java 的 main 方法
```

### 数据库操作（openGauss）

```bash
# 使用 Docker 启动 openGauss
docker run -d --name opengauss \
  -e GS_PASSWORD=Gaussdb@123 \
  -p 5432:5432 \
  enmotech/opengauss:5.1.0

# 连接数据库
docker exec -it opengauss gsql -U gaussdb -W Gaussdb@123

# 或使用 psql 客户端（openGauss 兼容 PostgreSQL）
psql -h localhost -p 5432 -U gaussdb -d oncoresi

# 初始化数据库（首次运行）
docker cp oncoresi-backend/oncoresi-infra/src/main/resources/db/init.sql opengauss:/tmp/
docker exec -it opengauss gsql -U gaussdb -W Gaussdb@123 -f /tmp/init.sql
```

### 工具命令

```bash
# 生成 BCrypt 密码哈希
cd oncoresi-backend/oncoresi-api
mvn exec:java -Dexec.mainClass="com.oncoresi.api.util.PasswordEncoderTest"
```

## 前端开发命令

### 安装依赖

```bash
cd oncoresi-frontend

# 首次安装（确保已安装 pnpm）
npm i -g corepack
pnpm install

# 清理并重装
pnpm clean
pnpm install
```

### 开发运行

```bash
# 启动开发服务器（Ant Design 版本）
pnpm dev:antd

# 或使用简化命令（如果已配置）
pnpm dev

# 指定端口启动
pnpm dev:antd -- --port 3000
```

### 构建打包

```bash
# 构建生产版本
pnpm build:antd

# 或
pnpm build

# 预览构建结果
pnpm preview
```

### 代码检查

```bash
# ESLint 检查
pnpm lint

# 格式化代码
pnpm format

# 类型检查
pnpm check:type
```

## 测试命令

### 后端测试（待实现）

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=AuthServiceTest

# 跳过测试
mvn install -DskipTests
```

### 前端测试

```bash
# 单元测试
pnpm test:unit

# E2E 测试
pnpm test:e2e
```

## API 测试命令

### 登录接口测试

```bash
# 使用管理员账号登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 使用教师账号登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher","password":"admin123"}'

# 使用学员账号登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student","password":"admin123"}'
```

### 认证接口测试

```bash
# 先获取 token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')

# 使用 token 访问受保护接口
curl -X GET http://localhost:8080/api/auth/test \
  -H "Authorization: Bearer $TOKEN"
```

## Git 操作命令

```bash
# 查看状态
git status

# 提交代码
git add .
git commit -m "feat(module): add new feature"

# 推送到远程
git push origin master

# 查看最近提交
git log --oneline -10
```

## Docker 命令（可选）

### 构建镜像

```bash
# 后端镜像
docker build -t oncoresi-backend:latest -f Dockerfile.backend .

# 前端镜像
docker build -t oncoresi-frontend:latest -f Dockerfile.frontend .
```

### 运行容器

```bash
# 启动 MySQL
docker run -d --name oncoresi-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=oncoresi \
  -p 3306:3306 \
  mysql:8.0

# 启动后端
docker run -d --name oncoresi-backend \
  -p 8080:8080 \
  --link oncoresi-mysql:db \
  oncoresi-backend:latest

# 启动前端
docker run -d --name oncoresi-frontend \
  -p 80:80 \
  oncoresi-frontend:latest
```

## 快速启动（完整流程）

### 首次启动

```bash
# 1. 启动 MySQL 并初始化数据库
mysql -u root -p < oncoresi-infra/src/main/resources/db/init.sql

# 2. 启动后端
cd oncoresi-backend/oncoresi-api
mvn spring-boot:run

# 3. 新开终端，启动前端
cd oncoresi-frontend
pnpm install
pnpm dev:antd

# 4. 访问应用
# 前端：http://localhost:5173
# 后端：http://localhost:8080/api
```

## 常用开发工作流

### 新增功能开发流程

```bash
# 1. 创建功能分支
git checkout -b feature/training-plan

# 2. 后端开发（边改边测）
cd oncoresi-backend/oncoresi-api
mvn spring-boot:run

# 3. 前端开发
cd oncoresi-frontend
pnpm dev:antd

# 4. 提交代码
git add .
git commit -m "feat(training): add training plan management"
git push origin feature/training-plan
```

## 访问地址

- **后端 API：** http://localhost:8080/api
- **Swagger UI：** http://localhost:8080/api/swagger-ui.html
- **API 文档：** http://localhost:8080/api/v3/api-docs
- **健康检查：** http://localhost:8080/api/actuator/health
- **Metrics：** http://localhost:8080/api/actuator/metrics
- **前端应用：** http://localhost:5173（开发时）

## 测试账号

```
用户名      密码         角色
-----------------------------------------------
admin      admin123    HOSPITAL_ADMIN（医院管理员）
teacher    admin123    SUPERVISOR, TEACHER（导师+教师）
student    admin123    TRAINEE（学员）
```

## 中间件操作

### Redis 操作

```bash
# 启动 Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 连接 Redis
docker exec -it redis redis-cli

# 查看 Sa-Token 的 Session 数据
keys satoken:*
```

### RocketMQ 操作

```bash
# 启动 RocketMQ（使用 docker-compose）
docker-compose up -d rocketmq-namesrv rocketmq-broker

# 查看 NameServer 日志
docker-compose logs rocketmq-namesrv

# 查看 Broker 日志
docker-compose logs rocketmq-broker
```

### AI 服务操作（可选，默认禁用）

```bash
# 安装 Ollama（仅在需要 AI 功能时）
curl -fsSL https://ollama.com/install.sh | sh

# 拉取中文大模型
ollama pull qwen2.5:7b

# 启动 Ollama 服务
ollama serve

# 测试 AI 服务
curl http://localhost:11434/api/tags

# 启用 AI 功能：修改 application.yml
# spring.ai.enabled: false → true
```
