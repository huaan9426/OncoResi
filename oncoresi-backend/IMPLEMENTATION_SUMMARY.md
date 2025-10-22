# JWT登录权限系统实现总结

## 实现概述

已完成基于DDD架构的JWT+RBAC权限系统,支持:
- ✅ 用户登录(用户名+密码)
- ✅ JWT token生成和验证
- ✅ 一个用户对应多个角色(1:N)
- ✅ 基于角色的权限控制
- ✅ Spring Security集成

---

## 代码结构分析

### 1. oncoresi-types (共享类型层)

**文件清单:**
- `LoginRequest.java` - 登录请求DTO
- `LoginResponse.java` - 登录响应DTO
- `Result.java` - 统一响应包装
- `UserRole.java` - 角色枚举(8种角色)

**职责:** 定义跨层共享的数据结构,无业务逻辑

---

### 2. oncoresi-domain (领域层)

**文件清单:**
- `entity/User.java` - 用户领域实体
- `entity/Role.java` - 角色领域实体
- `repository/UserRepository.java` - 用户仓储接口

**关键设计:**
```java
public class User {
    private Long id;
    private String username;
    private String password;
    private Integer status;
    private Set<Role> roles;  // 一对多关系

    // 业务方法
    public boolean isEnabled() { ... }
    public Set<String> getRoleCodes() { ... }
}
```

**职责:** 核心业务逻辑,不依赖任何技术框架

---

### 3. oncoresi-infra (基础设施层)

**文件清单:**
- `persistence/po/UserPO.java` - 用户JPA实体
- `persistence/po/RolePO.java` - 角色JPA实体
- `persistence/jpa/UserJpaRepository.java` - JPA接口
- `persistence/converter/UserConverter.java` - 领域对象与PO转换器
- `persistence/repository/UserRepositoryImpl.java` - 仓储实现

**关键设计:**
```java
@Entity
@Table(name = "sys_user")
public class UserPO {
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sys_user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RolePO> roles;  // JPA多对多映射
}
```

**职责:** 实现领域层定义的接口,处理数据持久化

---

### 4. oncoresi-application (应用层)

**文件清单:**
- `service/AuthService.java` - 认证应用服务
- `service/JwtService.java` - JWT工具服务

**关键设计:**
```java
@Service
public class AuthService {
    // 登录流程编排
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        // 2. 验证密码
        // 3. 检查状态
        // 4. 生成Token
        // 5. 返回结果
    }
}
```

**职责:** 服务编排、事务控制、DTO转换

---

### 5. oncoresi-api (接口层)

**文件清单:**
- `controller/AuthController.java` - 认证控制器
- `security/JwtAuthenticationFilter.java` - JWT认证过滤器
- `config/SecurityConfig.java` - Spring Security配置
- `util/PasswordEncoderTest.java` - 密码加密工具

**关键设计:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    protected void doFilterInternal(...) {
        // 1. 提取Token
        // 2. 验证Token
        // 3. 提取用户信息和角色
        // 4. 构建Authentication对象
        // 5. 设置到SecurityContext
    }
}
```

**职责:** HTTP请求处理、认证授权、异常处理

---

## 数据库设计

### 表结构

**sys_user (用户表)**
```sql
- id: BIGINT (主键)
- username: VARCHAR(50) (唯一索引)
- password: VARCHAR(200) (BCrypt加密)
- real_name: VARCHAR(50)
- phone: VARCHAR(20)
- email: VARCHAR(100)
- status: INT (1-启用, 0-禁用)
- create_time: DATETIME
- update_time: DATETIME
```

**sys_role (角色表)**
```sql
- id: BIGINT (主键)
- code: VARCHAR(50) (唯一索引, 如: HOSPITAL_ADMIN)
- name: VARCHAR(50) (如: 医院管理员)
- description: VARCHAR(200)
- create_time: DATETIME
```

**sys_user_role (用户角色关联表)**
```sql
- user_id: BIGINT (外键 → sys_user.id)
- role_id: BIGINT (外键 → sys_role.id)
- 联合主键: (user_id, role_id)
```

### 关系说明
- 用户 : 角色 = 1 : N (一个用户可以有多个角色)
- 级联删除: 删除用户时自动删除关联关系

---

## 认证流程

### 登录流程
```
1. 用户提交用户名+密码
   ↓
2. AuthService.login() 验证
   ↓
3. 验证通过后 JwtService.generateToken()
   ↓
4. 返回 LoginResponse (包含token和角色信息)
```

### 请求认证流程
```
1. 客户端请求带 Authorization: Bearer <token>
   ↓
2. JwtAuthenticationFilter 拦截
   ↓
3. 提取并验证Token
   ↓
4. 从Token中提取userId和roles
   ↓
5. 构建 UsernamePasswordAuthenticationToken
   ↓
6. 设置到 SecurityContextHolder
   ↓
7. 后续可通过 @PreAuthorize 进行权限控制
```

---

## 权限控制方案

### 1. 基于注解的方法级权限

```java
// 单个角色
@PreAuthorize("hasRole('HOSPITAL_ADMIN')")
public Result<?> adminOnly() { ... }

// 多个角色任一
@PreAuthorize("hasAnyRole('SUPERVISOR', 'TEACHER')")
public Result<?> teacherArea() { ... }

// 所有认证用户
@PreAuthorize("isAuthenticated()")
public Result<?> userArea() { ... }
```

### 2. 获取当前用户信息

```java
@GetMapping("/current")
public Result<UserInfo> getCurrentUser() {
    // 从SecurityContext获取userId
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Long userId = (Long) auth.getPrincipal();

    // 获取角色
    Set<String> roles = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

    return Result.success(new UserInfo(userId, roles));
}
```

---

## 设计原则与决策

### ✅ 遵循的原则

1. **DDD分层架构**
   - 领域层独立于技术框架
   - 依赖倒置:infra实现domain定义的接口
   - 清晰的职责边界

2. **单一职责**
   - JwtService只负责Token操作
   - AuthService只负责业务编排
   - Controller只负责HTTP处理

3. **向后兼容**
   - 使用标准的JWT和Spring Security
   - 数据库表设计预留扩展字段
   - 易于添加新角色

4. **最简可行方案**
   - 角色即权限,不引入复杂的RBAC
   - 使用Spring Security现成能力
   - 避免过度抽象

### ❌ 避免的陷阱

1. **没有引入复杂的权限模型**
   - 不使用独立的Permission表
   - 不使用资源-操作矩阵
   - 8种角色足够满足需求

2. **没有过度设计**
   - 不使用微内核架构
   - 不引入事件溯源
   - 不使用CQRS分离

3. **没有引入不必要的技术**
   - 不使用Redis(当前规模不需要)
   - 不使用消息队列
   - 不使用分布式Session

---

## 测试数据

### 测试账号

| 用户名 | 密码 | 角色 | 用例 |
|--------|------|------|------|
| admin | admin123 | HOSPITAL_ADMIN | 测试单角色 |
| teacher | admin123 | SUPERVISOR, TEACHER | 测试多角色 |
| student | admin123 | TRAINEE | 测试普通用户 |

---

## 扩展点

### 1. 添加新角色
```sql
-- 1. 在sys_role表添加新角色
INSERT INTO sys_role (code, name, description)
VALUES ('NEW_ROLE', '新角色', '描述');

-- 2. 给用户分配角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 9);
```

### 2. 细化权限控制
如果未来需要更细粒度的权限:
```java
// 可以在Role实体中添加permissions字段
public class Role {
    private Set<String> permissions; // 如: ["user:read", "user:write"]
}

// 在Filter中将permissions也加入Authentication
Set<SimpleGrantedAuthority> authorities = permissions.stream()
    .map(SimpleGrantedAuthority::new)
    .collect(Collectors.toSet());
```

### 3. Token刷新机制
```java
// 可以添加RefreshToken
public class JwtService {
    public String refreshToken(String oldToken) {
        if (validateToken(oldToken)) {
            Claims claims = getClaimsFromToken(oldToken);
            return generateNewToken(claims);
        }
        throw new RuntimeException("Token已失效");
    }
}
```

---

## 性能考虑

### 当前设计(小规模)
- 每次请求从Token中解析用户信息
- 不使用缓存
- 适合 < 1万用户

### 优化建议(中大规模)
```java
// 1. 添加Redis缓存用户信息
@Cacheable(value = "user", key = "#userId")
public User findById(Long userId) { ... }

// 2. 缓存Token验证结果
@Cacheable(value = "token", key = "#token", unless = "#result == false")
public boolean validateToken(String token) { ... }
```

---

## 安全建议

### 生产环境配置

```yaml
jwt:
  # 使用强随机字符串(至少32字节)
  secret: ${JWT_SECRET:请使用环境变量配置}
  # Token有效期(根据业务调整)
  expiration: 7200000  # 2小时

spring:
  datasource:
    # 使用连接池
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```

### 密码策略
- ✅ 使用BCrypt加密(已实现)
- ✅ 最小长度8位
- ⚠️ 建议添加密码强度检查
- ⚠️ 建议添加登录失败锁定

---

## 总结

这是一个**实用、简洁、可扩展**的JWT+RBAC实现:

**优点:**
1. ✅ 严格遵循DDD分层,易于维护
2. ✅ 支持一个用户多个角色(满足需求)
3. ✅ 标准Spring Security,易于理解
4. ✅ 无过度设计,代码简洁
5. ✅ 易于扩展(添加新角色、细化权限)

**适用场景:**
- 中小型企业应用
- 用户规模 < 10万
- 角色数量 < 50
- 不需要动态权限配置

**如需升级:**
- 添加Permission表实现RBAC
- 引入Redis缓存提升性能
- 添加Token刷新机制
- 实现动态权限配置UI

当前实现已经满足医师规培系统的基本需求,可以开始业务功能开发。
