# 部署指南

## 生产环境部署检查清单

### 安全配置

- [ ] **数据库凭证**：移除硬编码的 root/root，使用环境变量
- [ ] **JWT 密钥**：改为环境变量，使用强密钥（至少 256 位）
- [ ] **Hibernate DDL**：设置为 `validate`，不要用 `update`
- [ ] **HTTPS/TLS**：强制使用 HTTPS
- [ ] **日志级别**：改为 INFO 或 WARN，不要用 DEBUG
- [ ] **限流保护**：为 `/auth/login` 添加限流
- [ ] **刷新令牌**：实现 token 刷新机制
- [ ] **错误处理**：统一异常处理，不暴露敏感信息
- [ ] **CORS 策略**：配置正确的跨域白名单
- [ ] **监控告警**：配置 Spring Boot Actuator

### 性能优化

- [ ] **数据库连接池**：HikariCP 参数调优
- [ ] **Redis 缓存**：接入 Redis 缓存热点数据
- [ ] **静态资源 CDN**：前端静态资源使用 CDN
- [ ] **Gzip 压缩**：启用 HTTP 响应压缩
- [ ] **数据库索引**：检查并优化慢查询
- [ ] **API 分页**：列表接口强制分页

### 备份策略

- [ ] **数据库备份**：每日自动备份
- [ ] **配置文件备份**：版本控制外备份
- [ ] **日志归档**：日志文件定期归档
- [ ] **灾难恢复计划**：制定恢复流程

## 环境变量配置

### 后端环境变量

```bash
# application-prod.yml 中引用环境变量
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:validate}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:7200000}

server:
  port: ${SERVER_PORT:8080}
```

### 生产环境变量设置

```bash
# Linux / macOS
export DB_URL="jdbc:mysql://prod-db-server:3306/oncoresi?useSSL=true"
export DB_USERNAME="oncoresi_user"
export DB_PASSWORD="strong-password-here"
export JWT_SECRET=$(openssl rand -base64 64)
export JWT_EXPIRATION=7200000
export DDL_AUTO="validate"

# Windows
set DB_URL=jdbc:mysql://prod-db-server:3306/oncoresi?useSSL=true
set DB_USERNAME=oncoresi_user
set DB_PASSWORD=strong-password-here
set JWT_SECRET=your-generated-secret-key
set JWT_EXPIRATION=7200000
set DDL_AUTO=validate
```

## Docker 部署

### Dockerfile - 后端

```dockerfile
# oncoresi-backend/Dockerfile
FROM openjdk:8-jdk-alpine

# 设置工作目录
WORKDIR /app

# 复制 JAR 文件
COPY oncoresi-api/target/oncoresi-api-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Xms512m", \
  "-Xmx1024m", \
  "-jar", \
  "app.jar"]
```

### Dockerfile - 前端

```dockerfile
# oncoresi-frontend/Dockerfile
# 构建阶段
FROM node:20-alpine AS builder

WORKDIR /app

# 安装 pnpm
RUN npm i -g pnpm

# 复制依赖文件
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/web-antd/package.json ./apps/web-antd/
COPY packages/ ./packages/

# 安装依赖
RUN pnpm install --frozen-lockfile

# 复制源代码
COPY . .

# 构建
RUN pnpm build:antd

# 生产阶段
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/apps/web-antd/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### nginx.conf

```nginx
server {
    listen 80;
    server_name localhost;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    root /usr/share/nginx/html;
    index index.html;

    # Vue Router history 模式支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: oncoresi-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: oncoresi
      MYSQL_USER: ${DB_USERNAME}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
      - ./oncoresi-backend/oncoresi-infra/src/main/resources/db/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - oncoresi-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis 缓存（可选）
  redis:
    image: redis:7-alpine
    container_name: oncoresi-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - oncoresi-network
    command: redis-server --appendonly yes

  # 后端应用
  backend:
    build:
      context: ./oncoresi-backend
      dockerfile: Dockerfile
    container_name: oncoresi-backend
    restart: always
    environment:
      DB_URL: jdbc:mysql://mysql:3306/oncoresi?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: 7200000
      DDL_AUTO: validate
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - oncoresi-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # 前端应用
  frontend:
    build:
      context: ./oncoresi-frontend
      dockerfile: Dockerfile
    container_name: oncoresi-frontend
    restart: always
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - oncoresi-network

volumes:
  mysql-data:
  redis-data:

networks:
  oncoresi-network:
    driver: bridge
```

### .env 文件（不要提交到 Git）

```bash
# .env
DB_ROOT_PASSWORD=root-strong-password
DB_USERNAME=oncoresi_user
DB_PASSWORD=oncoresi-db-password
JWT_SECRET=your-very-long-jwt-secret-key-at-least-256-bits
```

## Docker 部署命令

```bash
# 构建镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 停止服务
docker-compose down

# 停止并删除数据
docker-compose down -v
```

## 传统部署（非 Docker）

### 后端部署

```bash
# 1. 构建 JAR
cd oncoresi-backend
mvn clean package -DskipTests

# 2. 上传到服务器
scp oncoresi-api/target/oncoresi-api-0.0.1-SNAPSHOT.jar user@server:/opt/oncoresi/

# 3. 创建 systemd 服务
sudo nano /etc/systemd/system/oncoresi-backend.service
```

**oncoresi-backend.service**

```ini
[Unit]
Description=OncoResi Backend Service
After=syslog.target network.target

[Service]
User=oncoresi
WorkingDirectory=/opt/oncoresi
ExecStart=/usr/bin/java -Xms512m -Xmx1024m \
  -Dspring.profiles.active=prod \
  -jar /opt/oncoresi/oncoresi-api-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=10

Environment="DB_URL=jdbc:mysql://localhost:3306/oncoresi"
Environment="DB_USERNAME=oncoresi_user"
Environment="DB_PASSWORD=strong-password"
Environment="JWT_SECRET=your-jwt-secret"
Environment="DDL_AUTO=validate"

[Install]
WantedBy=multi-user.target
```

```bash
# 4. 启动服务
sudo systemctl daemon-reload
sudo systemctl enable oncoresi-backend
sudo systemctl start oncoresi-backend

# 5. 查看状态和日志
sudo systemctl status oncoresi-backend
sudo journalctl -u oncoresi-backend -f
```

### 前端部署

```bash
# 1. 构建前端
cd oncoresi-frontend
pnpm install
pnpm build:antd

# 2. 上传到服务器
scp -r apps/web-antd/dist/* user@server:/var/www/oncoresi/

# 3. 配置 Nginx
sudo nano /etc/nginx/sites-available/oncoresi
```

**Nginx 配置（同上 nginx.conf）**

```bash
# 4. 启用站点
sudo ln -s /etc/nginx/sites-available/oncoresi /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## 数据库初始化

```bash
# 生产环境初始化（谨慎操作）
mysql -u root -p < oncoresi-infra/src/main/resources/db/init.sql

# 或分步执行
mysql -u root -p
CREATE DATABASE oncoresi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oncoresi;
SOURCE /path/to/init.sql;
```

## SSL/TLS 证书配置

### Let's Encrypt（免费证书）

```bash
# 安装 certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d oncoresi.yourdomain.com

# 自动续期
sudo certbot renew --dry-run
```

### Nginx HTTPS 配置

```nginx
server {
    listen 443 ssl http2;
    server_name oncoresi.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/oncoresi.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/oncoresi.yourdomain.com/privkey.pem;

    # SSL 优化
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # ... 其他配置同上
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name oncoresi.yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

## 监控和日志

### Spring Boot Actuator

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### 日志配置

```yaml
# application-prod.yml
logging:
  level:
    root: INFO
    com.oncoresi: INFO
  file:
    name: /var/log/oncoresi/application.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
```

## 性能优化建议

### 数据库连接池（HikariCP）

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### JVM 参数调优

```bash
java -Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/oncoresi/heapdump.hprof \
  -jar oncoresi-api.jar
```

## 故障排查

### 常见问题

1. **无法连接数据库**
   - 检查数据库是否启动
   - 检查连接字符串、用户名、密码
   - 检查防火墙规则

2. **JWT 验证失败**
   - 检查 JWT_SECRET 是否一致
   - 检查 token 是否过期
   - 检查系统时间是否同步

3. **内存溢出（OOM）**
   - 增加 JVM 堆内存 `-Xmx`
   - 检查是否有内存泄漏
   - 分析 heap dump

4. **响应缓慢**
   - 检查数据库慢查询
   - 开启 Redis 缓存
   - 优化接口逻辑

### 日志查看

```bash
# Docker 日志
docker-compose logs -f backend

# Systemd 日志
sudo journalctl -u oncoresi-backend -f

# 文件日志
tail -f /var/log/oncoresi/application.log
```

## 备份和恢复

### 数据库备份

```bash
# 备份
mysqldump -u root -p oncoresi > backup_$(date +%Y%m%d).sql

# 恢复
mysql -u root -p oncoresi < backup_20241201.sql
```

### 自动备份脚本

```bash
#!/bin/bash
# /opt/oncoresi/backup.sh

BACKUP_DIR="/backup/oncoresi"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份数据库
mysqldump -u root -p${DB_PASSWORD} oncoresi | gzip > ${BACKUP_DIR}/db_${DATE}.sql.gz

# 删除 30 天前的备份
find ${BACKUP_DIR} -name "db_*.sql.gz" -mtime +30 -delete
```

```bash
# 添加到 crontab
crontab -e
0 2 * * * /opt/oncoresi/backup.sh
```
