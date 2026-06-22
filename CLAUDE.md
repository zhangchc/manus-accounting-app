# keep-accounts 技术规约

## 项目映射（按需求类型选择，禁止全量分析）

| 需求类型 | 后端服务 | 前端项目 |
|----------|----------|----------|
| 后台 PC 管理端 | `keep-accounts-manage` | `keep-accounts-pc` |
| C 端小程序 | `keep-accounts-server` | `keep-accounts-wxapp` |

- 接到需求时，先判断是 PC 后台还是 C 端小程序，然后**只读取对应两个项目**的代码。
- **禁止一次性加载所有工程进行分析**，避免上下文浪费。

## 技术栈

### 核心框架

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 核心框架，禁止升级到 3.x（JDK 1.8 不兼容） |
| Java | 1.8 | JDK 版本，**禁止使用 8+ 特性**（var, 模块化, 文本块等） |
| Maven | 3.11.0 | 构建工具，禁止切换到 Gradle |

### 数据层

| 技术 | 版本 | 说明 |
|------|------|------|
| MyBatis-Plus | 3.5.5 | ORM，含逻辑删除、自动填充、分页。**单表查询使用 LambdaQueryWrapper / LambdaUpdateWrapper，禁止字符串拼接 SQL；多表关联查询使用 XML 形式编写 SQL** |
| MySQL | 8.0.33 | 关系数据库，**禁止使用存储过程、触发器、外键** |
| Druid | 1.2.21 | 阿里巴巴连接池 |

### 工具库

| 技术 | 版本 | 说明 |
|------|------|------|
| Lombok | — | 实体/DTO 统一使用 `@Data`，Mapper 使用 `@Mapper`，禁止手写 getter/setter |
| Hutool | 5.8.25 | 通用工具集，**涉及集合、日期、字符串操作时优先使用 Hutool，禁止引入 Guava / Apache Commons** |
| JWT (jjwt) | 0.9.1 | 无状态身份认证 |
| Spring AOP | — | Controller 入参/出参日志切面 + TraceId |
| Spring Validation | — | 请求参数校验，**禁止在 Controller 方法体内手动校验参数** |
| DashScope SDK | 2.22.12 | 阿里云百炼 AI SDK |

## 包结构规约

```
com.accounting
├── controller/    # 控制器，仅负责参数接收和结果返回，不写业务逻辑
├── service/       # 业务接口
│   └── impl/      # 业务实现，事务注解加在 impl 类上
├── mapper/        # MyBatis-Plus Mapper 接口，继承 BaseMapper<T>
├── entity/        # 数据库实体，使用 @TableName @TableId @TableField(@TableLogic)
├── dto/           # 请求入参 DTO，使用 @Valid 校验注解
├── vo/            # 响应出参 VO
├── config/        # 配置类（CORS、MyBatis-Plus、WebMvc 等）
├── utils/         # 工具类
└── common/        # 全局异常处理、统一响应体、拦截器
```

## 编码规约

### 实体规范
- entity 类必须使用 `@TableName` 指定表名
- 主键使用 `@TableId(type = IdType.ASSIGN_ID)` 雪花 ID
- 逻辑删除字段使用 `@TableLogic`
- 自动填充字段（createTime, updateTime）使用 `@TableField(fill = FieldFill.INSERT)` 等
- 禁止在 entity 中写业务逻辑

### Controller 规范
- 类级别使用 `@RestController` + `@RequestMapping`
- 参数校验使用 `@Valid` + 注解（`@NotBlank`, `@NotNull` 等），禁止在方法体内 if-else 校验
- 返回统一响应体 `Result<T>`，禁止直接返回 entity

### Service 规范
- 接口-实现分离，`@Transactional` 加在 impl 类的方法上，仅在涉及多表写入或需要保证数据一致性的场景使用，单表操作或只读查询无需添加
- 禁止在 service 中直接操作 HttpServletRequest/Response

### Mapper 规范
- 继承 `BaseMapper<T>`，禁止在 Mapper.xml 中定义 BaseMapper 已覆盖的 CRUD
- 复杂查询优先使用 `LambdaQueryWrapper`，禁止 SQL 字符串拼接

### 日志规范
- Controller 层入参/出参由 AOP 切面统一打印，禁止在 Controller 方法内手动 log
- 日志使用 Lombok `@Slf4j`，禁止 `System.out.println`

## 禁止事项

1. 禁止引入与已有工具库功能重复的依赖（Guava、Apache Commons 等，Hutool 已覆盖）
2. 禁止使用 Spring Boot 3.x 及 Jakarta EE（项目锁定 JDK 8 + Java EE）
3. 禁止在 entity 中直接返回给前端（必须经过 VO 转换）
4. 禁止硬编码魔法数字/字符串，统一使用常量或枚举
5. 禁止在 Mapper 中写复杂业务逻辑
6. 禁止使用 JPA/Hibernate（项目已锁定 MyBatis-Plus）
7. 禁止在 Controller 中捕获异常（由全局异常处理器统一处理）
