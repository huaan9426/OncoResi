# OncoResi Backend Dockerfile
# 基于 Java 21 的多阶段构建

# 构建阶段
FROM maven:3.9-amazoncorretto-21 AS builder

WORKDIR /build

# 复制 POM 文件并下载依赖（利用 Docker 缓存）
COPY pom.xml .
COPY oncoresi-types/pom.xml oncoresi-types/
COPY oncoresi-domain/pom.xml oncoresi-domain/
COPY oncoresi-infra/pom.xml oncoresi-infra/
COPY oncoresi-application/pom.xml oncoresi-application/
COPY oncoresi-api/pom.xml oncoresi-api/

RUN mvn dependency:go-offline -B

# 复制源代码
COPY oncoresi-types/ oncoresi-types/
COPY oncoresi-domain/ oncoresi-domain/
COPY oncoresi-infra/ oncoresi-infra/
COPY oncoresi-application/ oncoresi-application/
COPY oncoresi-api/ oncoresi-api/

# 构建项目
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM amazoncorretto:21-alpine

WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache tzdata curl

# 设置时区为中国
ENV TZ=Asia/Shanghai

# 从构建阶段复制 JAR 文件
COPY --from=builder /build/oncoresi-api/target/oncoresi-api-*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl --fail http://localhost:8080/api/actuator/health || exit 1

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
