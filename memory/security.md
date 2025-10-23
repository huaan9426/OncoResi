# 安全机制（2024 Sa-Token 版）

## Sa-Token 认证流程

> **注意：** 已从 Spring Security + JWT 切换到 Sa-Token 1.39.0

## 认证流程图

### 完整认证流程图

```
┌─────────┐                  ┌──────────────┐                ┌─────────────┐
│ 前端    │                  │  后端 API     │                │  数据库     │
└────┬────┘                  └──────┬───────┘                └──────┬──────┘
     │                              │                               │
     │ 1. POST /auth/login          │                               │
     │    {username, password}      │                               │
     ├─────────────────────────────>│                               │
     │                              │                               │
     │                              │ 2. 查询用户                    │
     │                              ├──────────────────────────────>│
     │                              │ SELECT * FROM sys_user         │
     │                              │   WHERE username = ?           │
     │                              │                               │
     │                              │ 3. 返回用户数据 (含密码哈希)   │
     │                              │<──────────────────────────────┤
     │                              │                               │
     │                              │ 4. BCrypt 验证密码             │
     │                              │    matches(input, hash)        │
     │                              │                               │
     │                              │ 5. 检查用户状态                │
     │                              │    user.isEnabled()            │
     │                              │                               │
     │                              │ 6. 生成 JWT Token             │
     │                              │    - userId                   │
     │                              │    - username                 │
     │                              │    - roles[]                  │
     │                              │    - expiration (24h)         │
     │                              │                               │
     │ 7. 返回 LoginResponse         │                               │
     │    {token, userId, roles}    │                               │
     │<─────────────────────────────┤                               │
     │                              │                               │
     │ 8. 保存 token 到 localStorage │                               │
     │                              │                               │
     │ 9. GET /xxx/resource          │                               │
     │    Header: Authorization      │                               │
     │           Bearer <token>     │                               │
     ├─────────────────────────────>│                               │
     │                              │                               │
     │                              │ 10. JwtAuthenticationFilter   │
     │                              │     - 提取 token              │
     │                              │     - 验证签名                │
     │                              │     - 检查过期                │
     │                              │     - 提取 claims             │
     │                              │     - 设置 SecurityContext    │
     │                              │                               │
     │                              │ 11. Controller 方法执行       │
     │                              │     @PreAuthorize 检查        │
     │                              │                               │
     │ 12. 返回资源数据              │                               │
     │<─────────────────────────────┤                               │
     │                              │                               │
```

## Sa-Token 配置详解

### Token 结构

Sa-Token 使用更简单的 Token 机制，默认 UUID 格式：

```
Token 示例：3c7f0b8a-4e9d-4a5b-8f3e-1234567890ab

Token 存储：Redis
键格式：satoken:login:token:{tokenValue}
值：用户登录信息（userId、username、roles等）
```

### Sa-Token 配置参数

```yaml
# application.yml
sa-token:
  # Token 名称（header 中的 key）
  token-name: Authorization
  # Token 前缀
  token-prefix: Bearer
  # Token 有效期（秒）- 24 小时
  timeout: 86400
  # 是否允许同一账号多地登录
  is-concurrent: true
  # Token 风格（uuid、simple-uuid、random-32等）
  token-style: uuid
  # 是否输出操作日志
  is-log: true
  # Token 持久化方式（使用 Redis）
  is-print: false
```

### 生产环境配置

```yaml
# application-prod.yml
sa-token:
  timeout: 7200        # 2 小时（更短更安全）
  is-log: false        # 生产环境关闭日志
  is-concurrent: false # 禁止多地登录（提升安全）
```

## 角色权限体系

### 八种用户角色

| 角色代码 | 角色名称 | 权限范围 | 典型功能 |
|---------|---------|---------|---------|
| **HOSPITAL_ADMIN** | 医院管理员 | 全局 | 系统配置、用户管理、全局报表 |
| **BASE_ADMIN** | 专业基地管理员 | 基地级 | 基地管理、培训计划、绩效考核 |
| **DEPT_ADMIN** | 科室管理员 | 科室级 | 科室学员管理、轮转安排 |
| **SUPERVISOR** | 责任导师 | 学员级 | 学员指导、评价考核 |
| **TEACHER** | 带教老师 | 学员级 | 日常带教、病例指导 |
| **TRAINEE** | 学员 | 个人 | 学习记录、考试、病例提交 |
| **NURSE_EVALUATOR** | 护士评价 | 评价 | 评价学员护理能力 |
| **PATIENT_EVALUATOR** | 病人评价 | 评价 | 评价学员服务态度 |

### 角色权限映射

```java
// UserRole.java
public enum UserRole {
    HOSPITAL_ADMIN("HOSPITAL_ADMIN", "医院管理员"),
    BASE_ADMIN("BASE_ADMIN", "专业基地管理员"),
    DEPT_ADMIN("DEPT_ADMIN", "科室管理员"),
    SUPERVISOR("SUPERVISOR", "责任导师"),
    TEACHER("TEACHER", "带教老师"),
    TRAINEE("TRAINEE", "学员"),
    NURSE_EVALUATOR("NURSE_EVALUATOR", "护士评价"),
    PATIENT_EVALUATOR("PATIENT_EVALUATOR", "病人评价");
}
```

### 控制器权限控制

```java
@RestController
@RequestMapping("/training-plans")
public class TrainingPlanController {

    // 只有管理员可以创建培训计划
    @PostMapping
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'BASE_ADMIN', 'DEPT_ADMIN')")
    public Result<Long> create(@RequestBody CreateTrainingPlanRequest request) {
        // ...
    }

    // 所有角色都可以查看培训计划
    @GetMapping("/{id}")
    public Result<TrainingPlanResponse> getById(@PathVariable Long id) {
        // ...
    }

    // 只有学员和导师可以查看学习记录
    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('TRAINEE', 'SUPERVISOR', 'TEACHER')")
    public Result<List<RecordResponse>> getRecords() {
        // ...
    }
}
```

### 前端路由权限

```typescript
// router/routes/modules/training.ts
{
  path: '/training/plans',
  name: 'TrainingPlans',
  component: () => import('@/views/training/plans/index.vue'),
  meta: {
    title: '培训计划管理',
    // 只有这些角色可以访问此路由
    roles: ['HOSPITAL_ADMIN', 'BASE_ADMIN', 'DEPT_ADMIN'],
  },
}
```

## Sa-Token 配置

### SaTokenConfig.java 核心配置

```java
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定路由需要登录认证（排除登录、Swagger 等公开接口）
            SaRouter.match("/**")
                    .notMatch(
                            "/auth/login",       // 登录接口
                            "/auth/register",    // 注册接口
                            "/swagger-ui/**",    // Swagger UI
                            "/v3/api-docs/**",   // OpenAPI 文档
                            "/actuator/**",      // 监控端点
                            "/error"             // 错误页面
                    )
                    .check(r -> StpUtil.checkLogin());  // 登录校验
        })).addPathPatterns("/**");
    }
}
```

### Sa-Token 认证流程（简化版）

```java
// 1. 登录
StpUtil.login(userId);              // 记录登录状态
String token = StpUtil.getTokenValue();  // 获取 token

// 2. 存储 Session 数据
StpUtil.getSession().set("user", user);
StpUtil.getSession().set("roles", roleCodes);

// 3. 校验登录
StpUtil.checkLogin();               // 校验是否登录（抛异常）
boolean isLogin = StpUtil.isLogin(); // 校验是否登录（返回布尔）

// 4. 获取登录信息
Long userId = StpUtil.getLoginIdAsLong();
Object data = StpUtil.getSession().get("user");

// 5. 登出
StpUtil.logout();                   // 清除登录状态
```

**相比 Spring Security 的优势：**
- ✅ 代码量减少 70%
- ✅ 不需要自定义 Filter
- ✅ 注解式鉴权更简洁（`@SaCheckLogin`、`@SaCheckRole`）
- ✅ Session 管理更灵活
- ✅ 开箱即用，配置简单

## 密码安全

### BCrypt 密码加密

```java
// 加密密码（注册时）
String rawPassword = "admin123";
String hashedPassword = passwordEncoder.encode(rawPassword);
// 结果：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH

// 验证密码（登录时）
boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
```

### 密码策略（建议）

```java
// 密码复杂度验证
public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final String PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$";

    public static boolean validate(String password) {
        if (password.length() < MIN_LENGTH) {
            return false;
        }
        return password.matches(PATTERN);
    }
}
```

## 安全最佳实践

### 1. JWT 密钥管理

```bash
# ❌ 错误：硬编码在代码中
private static final String SECRET_KEY = "my-secret-key";

# ✅ 正确：使用环境变量
@Value("${jwt.secret}")
private String jwtSecret;

# 生产环境设置
export JWT_SECRET=$(openssl rand -base64 64)
```

### 2. Token 过期时间

```java
// 开发环境：24 小时
private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

// 生产环境：更短（2小时），配合刷新令牌
private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000;    // 2 小时
private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 天
```

### 3. 敏感信息保护

```yaml
# application-prod.yml（生产环境配置）
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}

# ⚠️ 不要提交到 Git
```

### 4. HTTPS 强制

```java
// 生产环境强制 HTTPS
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.requiresChannel()
            .anyRequest()
            .requiresSecure();  // 强制 HTTPS
        return http.build();
    }
}
```

### 5. 限流防护

```java
// 登录接口限流（防暴力破解）
@PostMapping("/login")
@RateLimiter(name = "login", fallbackMethod = "loginFallback")
public Result<LoginResponse> login(@RequestBody LoginRequest request) {
    // ...
}

// 限流配置（resilience4j）
resilience4j.ratelimiter:
  instances:
    login:
      limitForPeriod: 5        # 5 次
      limitRefreshPeriod: 60s  # 每 60 秒
```

## 常见安全问题

### 1. Token 泄露
**风险：** Token 被截获，攻击者可以伪装成用户
**防护：**
- 使用 HTTPS
- Token 存储在 localStorage（不要存在 Cookie）
- 设置合理的过期时间
- 实现刷新令牌机制

### 2. XSS 攻击
**风险：** 恶意脚本窃取 Token
**防护：**
- 前端对用户输入进行转义
- 使用 Vue 的 v-text 而非 v-html
- 设置 CSP (Content Security Policy)

### 3. SQL 注入
**风险：** 恶意 SQL 语句
**防护：**
- 使用 JPA 预编译查询（已实现）
- 永远不要拼接 SQL 字符串
- 参数验证

### 4. 权限提升
**风险：** 普通用户执行管理员操作
**防护：**
- 所有敏感接口添加 @PreAuthorize
- 后端验证，不信任前端
- 验证资源所属关系

## 安全配置文件位置

- **Spring Security 配置：** `oncoresi-api/src/main/java/com/oncoresi/api/config/SecurityConfig.java`
- **JWT 过滤器：** `oncoresi-api/src/main/java/com/oncoresi/api/security/JwtAuthenticationFilter.java`
- **JWT 服务：** `oncoresi-application/src/main/java/com/oncoresi/application/service/JwtService.java`
- **认证服务：** `oncoresi-application/src/main/java/com/oncoresi/application/service/AuthService.java`
