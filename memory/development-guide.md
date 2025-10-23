# 开发指南

## DDD 开发模式

### 新增功能的五层开发流程

以"培训计划管理"为例：

#### 1. Domain 层（领域层）

```java
// oncoresi-domain/src/main/java/com/oncoresi/domain/entity/TrainingPlan.java
public class TrainingPlan {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 业务逻辑方法
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }
}

// oncoresi-domain/src/main/java/com/oncoresi/domain/repository/TrainingPlanRepository.java
public interface TrainingPlanRepository {
    TrainingPlan findById(Long id);
    List<TrainingPlan> findAll();
    void save(TrainingPlan plan);
    void delete(Long id);
}
```

#### 2. Types 层（类型层）

```java
// oncoresi-types/src/main/java/com/oncoresi/types/dto/CreateTrainingPlanRequest.java
public class CreateTrainingPlanRequest {
    @NotBlank(message = "计划名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}

// oncoresi-types/src/main/java/com/oncoresi/types/dto/TrainingPlanResponse.java
public class TrainingPlanResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean active;
}
```

#### 3. Infra 层（基础设施层）

```java
// oncoresi-infra/src/main/java/com/oncoresi/infra/persistence/po/TrainingPlanPO.java
@Entity
@Table(name = "training_plan")
public class TrainingPlanPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}

// oncoresi-infra/src/main/java/com/oncoresi/infra/persistence/jpa/TrainingPlanJpaRepository.java
public interface TrainingPlanJpaRepository extends JpaRepository<TrainingPlanPO, Long> {
    // Spring Data 自动实现 CRUD
}

// oncoresi-infra/src/main/java/com/oncoresi/infra/persistence/converter/TrainingPlanConverter.java
@Component
public class TrainingPlanConverter {
    public TrainingPlan toDomain(TrainingPlanPO po) {
        // PO → Domain
    }

    public TrainingPlanPO toPO(TrainingPlan domain) {
        // Domain → PO
    }
}

// oncoresi-infra/src/main/java/com/oncoresi/infra/persistence/repository/TrainingPlanRepositoryImpl.java
@Repository
public class TrainingPlanRepositoryImpl implements TrainingPlanRepository {
    @Autowired
    private TrainingPlanJpaRepository jpaRepository;

    @Autowired
    private TrainingPlanConverter converter;

    @Override
    public TrainingPlan findById(Long id) {
        return jpaRepository.findById(id)
            .map(converter::toDomain)
            .orElse(null);
    }

    @Override
    public void save(TrainingPlan plan) {
        TrainingPlanPO po = converter.toPO(plan);
        jpaRepository.save(po);
    }
}
```

#### 4. Application 层（应用层）

```java
// oncoresi-application/src/main/java/com/oncoresi/application/service/TrainingPlanService.java
@Service
@Transactional
public class TrainingPlanService {
    @Autowired
    private TrainingPlanRepository trainingPlanRepository;

    public Long createPlan(CreateTrainingPlanRequest request) {
        // 1. 业务验证
        validatePlanTime(request.getStartTime(), request.getEndTime());

        // 2. 创建领域对象
        TrainingPlan plan = new TrainingPlan();
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setStartTime(request.getStartTime());
        plan.setEndTime(request.getEndTime());

        // 3. 保存
        trainingPlanRepository.save(plan);

        return plan.getId();
    }

    public TrainingPlanResponse getById(Long id) {
        TrainingPlan plan = trainingPlanRepository.findById(id);
        if (plan == null) {
            throw new RuntimeException("培训计划不存在");
        }

        // 转换为 DTO
        return convertToResponse(plan);
    }

    private void validatePlanTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new RuntimeException("结束时间不能早于开始时间");
        }
    }

    private TrainingPlanResponse convertToResponse(TrainingPlan plan) {
        TrainingPlanResponse response = new TrainingPlanResponse();
        response.setId(plan.getId());
        response.setName(plan.getName());
        response.setDescription(plan.getDescription());
        response.setStartTime(plan.getStartTime());
        response.setEndTime(plan.getEndTime());
        response.setActive(plan.isActive());
        return response;
    }
}
```

#### 5. API 层（接口层）

```java
// oncoresi-api/src/main/java/com/oncoresi/api/controller/TrainingPlanController.java
@RestController
@RequestMapping("/training-plans")
public class TrainingPlanController {
    @Autowired
    private TrainingPlanService trainingPlanService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'BASE_ADMIN', 'DEPT_ADMIN')")
    public Result<Long> create(@Valid @RequestBody CreateTrainingPlanRequest request) {
        Long id = trainingPlanService.createPlan(request);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    public Result<TrainingPlanResponse> getById(@PathVariable Long id) {
        TrainingPlanResponse response = trainingPlanService.getById(id);
        return Result.success(response);
    }

    @GetMapping
    public Result<List<TrainingPlanResponse>> list() {
        List<TrainingPlanResponse> list = trainingPlanService.listAll();
        return Result.success(list);
    }
}
```

## 开发原则

### 1. DDD 分层原则
- **API 层禁止直接访问 Infra 层**：必须通过 Application 层
- **领域层纯粹性**：Domain 层不依赖任何框架（除验证注解）
- **依赖倒置**：Domain 定义接口，Infra 实现接口
- **单向依赖**：依赖方向始终向内（从外到内）

### 2. 聚合和事务
- **一个事务操作一个聚合根**：不要在一个事务中操作多个聚合
- **跨聚合通过事件**：聚合间通过领域事件通信（待实现）
- **事务边界在 Application 层**：使用 @Transactional

### 3. DTO 转换
- **Application 层负责转换**：在 Service 中完成 Domain ↔ DTO 转换
- **Controller 不做业务逻辑**：只做参数验证和委托
- **避免 DTO 污染 Domain**：Domain 实体不知道 DTO 的存在

### 4. 仓储模式
- **所有数据库访问通过 Repository**：不直接使用 JpaRepository
- **Repository 接口在 Domain**：实现在 Infra
- **返回领域对象**：Repository 返回 Domain Entity，不是 PO

### 5. 命名规范
- **Entity 命名**：User、Role、TrainingPlan（领域概念）
- **PO 命名**：UserPO、RolePO、TrainingPlanPO（加 PO 后缀）
- **DTO 命名**：LoginRequest、LoginResponse、CreateXxxRequest、XxxResponse
- **Service 命名**：AuthService、TrainingPlanService（业务概念 + Service）
- **Controller 命名**：AuthController、TrainingPlanController（资源 + Controller）

## 前端开发规范

### API 调用封装

```typescript
// apps/web-antd/src/api/training-plan.ts
import { request } from '@vben/request';

export interface CreateTrainingPlanRequest {
  name: string;
  description?: string;
  startTime: string;
  endTime: string;
}

export interface TrainingPlanResponse {
  id: number;
  name: string;
  description?: string;
  startTime: string;
  endTime: string;
  active: boolean;
}

// 创建培训计划
export function createTrainingPlan(data: CreateTrainingPlanRequest) {
  return request.post<number>('/training-plans', data);
}

// 获取培训计划详情
export function getTrainingPlan(id: number) {
  return request.get<TrainingPlanResponse>(`/training-plans/${id}`);
}

// 获取培训计划列表
export function listTrainingPlans() {
  return request.get<TrainingPlanResponse[]>('/training-plans');
}
```

### 路由配置

```typescript
// apps/web-antd/src/router/routes/modules/training.ts
import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/training',
    name: 'Training',
    meta: {
      title: '培训管理',
      icon: 'carbon:training',
      roles: ['HOSPITAL_ADMIN', 'BASE_ADMIN', 'DEPT_ADMIN'],
    },
    children: [
      {
        path: 'plans',
        name: 'TrainingPlans',
        component: () => import('@/views/training/plans/index.vue'),
        meta: {
          title: '培训计划',
        },
      },
    ],
  },
];

export default routes;
```

## 常见问题

### 1. 模块依赖问题
**问题：** 构建失败提示 "cannot find symbol"
**解决：**
- 检查 pom.xml 中的模块依赖顺序
- 确保依赖方向正确（api → application → domain/infra）
- 运行 `mvn clean install` 重新构建

### 2. JPA vs Domain 混淆
**问题：** 不知道什么时候用 UserPO，什么时候用 User
**解决：**
- **UserPO**：只在 infra 层使用，用于 JPA 持久化
- **User**：在 domain、application、api 层使用，是业务实体
- **转换**：使用 Converter 在两者之间转换

### 3. JWT 密钥管理
**问题：** JWT 密钥硬编码在代码中
**解决：**
- 开发环境：可以硬编码（当前方式）
- 生产环境：必须使用环境变量
```java
@Value("${jwt.secret}")
private String jwtSecret;
```

### 4. 数据库自动更新
**问题：** Hibernate ddl-auto 设置
**解决：**
- **开发环境**：使用 `update`（自动更新表结构）
- **生产环境**：使用 `validate`（只验证，不修改）
- **初始化**：使用 init.sql 脚本

### 5. CORS 跨域问题
**问题：** 前端无法调用后端 API
**解决：**
在 SecurityConfig 中添加 CORS 配置
```java
@Bean
public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("http://localhost:5173");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
}
```

## 代码检查清单

### 新增功能提交前
- [ ] Domain 层不包含框架依赖
- [ ] Repository 接口在 Domain，实现在 Infra
- [ ] Application 层使用 @Transactional
- [ ] API 层参数添加 @Valid 验证
- [ ] 敏感操作添加 @PreAuthorize 权限控制
- [ ] DTO 有合适的验证注解
- [ ] 异常有清晰的错误信息
- [ ] 代码有必要的注释（特别是复杂业务逻辑）

## 文档参考

- **后端详细文档：** oncoresi-backend/README.md
- **领域设计文档：** oncoresi-backend/DOMAIN_DESIGN.md
- **实现总结：** oncoresi-backend/IMPLEMENTATION_SUMMARY.md
- **API 测试指南：** oncoresi-backend/LOGIN_TEST.md
- **前端精简指南：** oncoresi-frontend/CLEANUP_GUIDE.md
