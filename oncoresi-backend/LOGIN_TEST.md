# 登录功能测试文档

## 启动前准备

### 1. 创建数据库
执行 `oncoresi-infra/src/main/resources/db/init.sql` 初始化数据库

```bash
mysql -u root -p < oncoresi-infra/src/main/resources/db/init.sql
```

### 2. 配置数据库连接
复制 `oncoresi-api/src/main/resources/application-dev.yml` 并修改数据库连接信息

### 3. 启动项目
```bash
cd oncoresi-api
mvn spring-boot:run
```

---

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | HOSPITAL_ADMIN | 医院管理员 |
| teacher | admin123 | SUPERVISOR, TEACHER | 责任导师+带教老师(一对多) |
| student | admin123 | TRAINEE | 学员 |

---

## API测试

### 1. 登录接口

**请求:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**成功响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 1,
    "username": "admin",
    "roles": ["HOSPITAL_ADMIN"]
  }
}
```

**失败响应:**
```json
{
  "code": 500,
  "message": "用户名或密码错误"
}
```

---

### 2. 测试认证接口

**请求(需要带Token):**
```bash
curl -X GET http://localhost:8080/api/auth/test \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**成功响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": "认证成功"
}
```

**未认证响应(401):**
```json
{
  "timestamp": "2025-01-20T10:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "path": "/api/auth/test"
}
```

---

## 测试多角色用户

**测试teacher用户(拥有2个角色):**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teacher",
    "password": "admin123"
  }'
```

**响应(注意roles是数组):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "...",
    "userId": 2,
    "username": "teacher",
    "roles": ["SUPERVISOR", "TEACHER"]
  }
}
```

---

## 权限验证

在Controller方法上使用 `@PreAuthorize` 注解进行权限控制:

```java
// 只有医院管理员可以访问
@PreAuthorize("hasRole('HOSPITAL_ADMIN')")
@GetMapping("/admin-only")
public Result<String> adminOnly() {
    return Result.success("管理员专属");
}

// 多个角色之一即可访问
@PreAuthorize("hasAnyRole('SUPERVISOR', 'TEACHER')")
@GetMapping("/teacher-area")
public Result<String> teacherArea() {
    return Result.success("教师区域");
}
```

---

## 数据库表结构

### sys_user (用户表)
- id: 用户ID
- username: 用户名(唯一)
- password: 密码(BCrypt加密)
- real_name: 真实姓名
- phone: 手机号
- email: 邮箱
- status: 状态(1-启用, 0-禁用)
- create_time: 创建时间
- update_time: 更新时间

### sys_role (角色表)
- id: 角色ID
- code: 角色代码(唯一)
- name: 角色名称
- description: 描述
- create_time: 创建时间

### sys_user_role (用户角色关联表)
- user_id: 用户ID (外键)
- role_id: 角色ID (外键)
- 联合主键: (user_id, role_id)

**关系:** 一个用户可以有多个角色(1:N)

---

## 故障排查

### 1. 密码加密问题
如果登录失败,检查数据库中的密码是否正确加密:
```java
// 生成BCrypt密码
String rawPassword = "admin123";
String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
System.out.println(encodedPassword);
```

### 2. Token验证失败
- 检查 `application.yml` 中的 `jwt.secret` 配置
- 确认Token格式: `Bearer <token>`
- 检查Token是否过期(默认24小时)

### 3. 角色权限不生效
- 确认 `@EnableGlobalMethodSecurity(prePostEnabled = true)` 已开启
- 角色前缀必须是 `ROLE_`,Spring Security会自动添加
- 数据库中的角色code不要带 `ROLE_` 前缀

---

## 架构说明

**DDD分层实现:**

1. **oncoresi-types**: DTO定义(LoginRequest, LoginResponse, Result)
2. **oncoresi-domain**: 领域实体(User, Role)和仓储接口(UserRepository)
3. **oncoresi-infra**: JPA实体(UserPO, RolePO)和仓储实现(UserRepositoryImpl)
4. **oncoresi-application**: 业务服务(AuthService, JwtService)
5. **oncoresi-api**: REST接口(AuthController)和安全配置(SecurityConfig, JwtAuthenticationFilter)

**依赖流向:**
```
api → application → domain ← infra
         ↓
      types
```

所有层都不依赖具体实现,只依赖接口,符合DDD和依赖倒置原则。
