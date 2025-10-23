# 数据权限设计文档

## 📌 设计目标

实现细粒度的数据权限控制，不同角色只能查看被授权的数据。

---

## 🏥 组织架构

### 两级结构（无基地）
```
医院（Hospital）
 └── 科室（Department）
      ├── 科室管理员
      ├── 责任导师
      ├── 带教老师
      └── 学员
```

### 角色 vs 数据权限
- **角色（Role）**：决定功能权限（能做什么）
- **数据权限（Data Scope）**：决定数据范围（能看什么）

---

## 🔐 四种数据权限类型

| 权限类型 | 代码 | 说明 | 典型角色 | SQL示例 |
|---------|------|------|---------|--------|
| 全院权限 | ALL | 查看全院所有数据 | 医院管理员 | 无过滤条件 |
| 科室权限 | DEPT | 查看本科室数据 | 科室管理员、教师 | WHERE dept_id = ? |
| 带教权限 | SUPERVISED | 查看带教学员数据 | 责任导师 | WHERE trainee_id IN (...) |
| 个人权限 | SELF | 只看个人数据 | 学员 | WHERE user_id = ? |

---

## 🔧 技术实现

### 1. 数据库设计

#### sys_user_data_scope（数据权限表）
```sql
CREATE TABLE sys_user_data_scope (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    scope_type VARCHAR(20) NOT NULL,  -- ALL/DEPT/SUPERVISED/SELF
    dept_id BIGINT,                   -- DEPT类型时使用
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);
```

#### sys_supervisor_trainee（导师-学员关联）
```sql
CREATE TABLE sys_supervisor_trainee (
    supervisor_id BIGINT NOT NULL,
    trainee_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    PRIMARY KEY (supervisor_id, trainee_id)
);
```

### 2. MyBatis-Flex 数据权限拦截器

#### 核心流程
```
1. Web请求 → DataScopeContextInterceptor
   ├─ 加载当前用户数据权限
   └─ 设置到ThreadLocal

2. SQL执行 → DataScopeInterceptor（MyBatis）
   ├─ 解析SQL
   ├─ 根据权限类型添加WHERE条件
   └─ 执行过滤后的SQL

3. 请求结束 → 清除ThreadLocal
```

#### 自动SQL过滤示例

**原始SQL:**
```sql
SELECT * FROM sys_user
```

**科室权限自动变为:**
```sql
SELECT * FROM sys_user WHERE dept_id = 1
```

**导师权限自动变为:**
```sql
SELECT * FROM sys_user
WHERE id IN (SELECT trainee_id FROM sys_supervisor_trainee WHERE supervisor_id = 2)
```

### 3. 代码实现

#### DataScopeContext（权限上下文）
```java
public class DataScopeContext {
    private Long userId;
    private DataScopeType scopeType;  // ALL/DEPT/SUPERVISED/SELF
    private Long deptId;
    private Long hospitalId;
}
```

#### DataScopeService（权限加载）
```java
@Service
public class DataScopeService {
    public DataScopeContext loadUserDataScope(Long userId) {
        // 查询用户数据权限配置
        DataScopePO dataScope = dataScopeMapper.selectByUserId(userId);
        // 构建权限上下文
        return buildContext(dataScope);
    }
}
```

#### DataScopeInterceptor（SQL拦截）
```java
@Intercepts({@Signature(type = Executor.class, method = "query", ...)})
public class DataScopeInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) {
        // 1. 获取权限上下文
        DataScopeContext context = SecurityContextHolder.getDataScopeContext();

        // 2. 解析SQL
        String sql = boundSql.getSql();

        // 3. 添加权限过滤
        String filteredSql = addDataScopeFilter(sql, context);

        // 4. 执行过滤后的SQL
        return invocation.proceed();
    }
}
```

---

## 📊 权限矩阵

| 角色 | 数据权限 | 查看用户 | 查看培训计划 | 查看考试成绩 |
|------|---------|---------|------------|------------|
| 医院管理员 | ALL | 全院用户 | 全院计划 | 全院成绩 |
| 科室管理员 | DEPT | 本科室用户 | 本科室计划 | 本科室成绩 |
| 责任导师 | SUPERVISED | 带教学员 | 学员计划 | 学员成绩 |
| 带教老师 | DEPT | 本科室用户 | 本科室计划 | 本科室成绩 |
| 学员 | SELF | 仅自己 | 仅自己的计划 | 仅自己的成绩 |

---

## 🎯 使用场景

### 场景1：科室管理员查询学员列表
```java
@GetMapping("/trainees")
@SaCheckRole("DEPT_ADMIN")
public List<User> listTrainees() {
    // 自动过滤：只返回本科室学员
    return traineeMapper.selectAll();
}
```

### 场景2：导师查看带教学员成绩
```java
@GetMapping("/my-students/scores")
@SaCheckRole("SUPERVISOR")
public List<ExamScore> getStudentScores() {
    // 自动过滤：只返回带教学员的成绩
    return scoreMapper.selectAll();
}
```

### 场景3：学员查看个人数据
```java
@GetMapping("/my-records")
@SaCheckRole("TRAINEE")
public List<TrainingRecord> getMyRecords() {
    // 自动过滤：只返回当前用户的记录
    return recordMapper.selectAll();
}
```

---

## 🛡️ 安全保障

### 1. 多层防护
- ✅ Web拦截器（加载权限）
- ✅ MyBatis拦截器（SQL过滤）
- ✅ 后端验证（业务逻辑）

### 2. 防止绕过
- ✅ 所有查询自动过滤
- ✅ ThreadLocal隔离（线程安全）
- ✅ 请求结束自动清理

### 3. 性能优化
- ✅ 权限上下文缓存（ThreadLocal）
- ✅ SQL解析缓存
- ✅ 索引优化（dept_id, user_id）

---

## 📁 关键文件位置

### 数据权限核心
- `DataScopeContext.java` - 权限上下文
- `DataScopeService.java` - 权限加载
- `DataScopeInterceptor.java` - SQL拦截器
- `DataScopeContextInterceptor.java` - Web拦截器

### 配置
- `DataPermissionConfig.java` - MyBatis拦截器注册
- `SaTokenConfig.java` - Web拦截器注册

### 数据库
- `init.sql` - 权限表初始化

---

## 🚀 扩展方向

### 1. 自定义数据权限
- 支持用户级自定义规则
- 动态权限配置

### 2. 细粒度控制
- 字段级权限（脱敏）
- 操作级权限（只读/读写）

### 3. 审计日志
- 记录数据访问日志
- 权限变更审计

---

**设计完成时间**: 2024年（适用于医疗、教育等多租户场景）
