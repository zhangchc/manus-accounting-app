# 轻记账项目 - 前后端代码分析与优化建议

结合《阿里巴巴 Java 开发手册》及前端常见规范，对项目进行逐项分析并给出可落地的优化点。

---

## 一、后端优化建议

### 1. 命名与常量（对应规范：强制 - 常量、魔法值）

**问题：**
- 代码中大量使用魔法值：`type == 1`（支出）、`type == 2`（收入）、`isDefault == 1` 等，不符合「不允许任何魔法值直接出现在代码中」。
- 缺少统一的类型/状态常量或枚举。

**建议：**
- 新增常量类或枚举，例如：

```java
// 建议：com.accounting.common.enums.RecordTypeEnum
public enum RecordTypeEnum {
    EXPENSE(1, "支出"),
    INCOME(2, "收入");
    private final int code;
    private final String desc;
    // getter、fromCode 等
}

// 建议：com.accounting.common.constants.CommonConstants 或实体内常量
public static final int DEFAULT_BOOK_YES = 1;
public static final int DEFAULT_BOOK_NO = 0;
```

- 在 `RecordServiceImpl`、`BookServiceImpl`、`RecordDTO` 校验等处用枚举或常量替代 `1`、`2` 和 `isDefault` 的 0/1。

**涉及文件：**
- `RecordServiceImpl.java`（type 判断）
- `BookServiceImpl.java`（isDefault 判断）
- `RecordDTO.java`（可增加 `@AssertTrue` 或自定义校验校验 type∈{1,2}）

---

### 2. 异常与事务（对应规范：异常处理、事务回滚）

**已做得好的：**
- `UserServiceImpl` 中 `@Transactional(rollbackFor = Exception.class)` 已正确指定回滚。
- `GlobalExceptionHandler` 统一处理业务异常与参数校验。

**可优化：**
- **JwtUtil.parseToken**：捕获 `Exception` 后直接返回 `null`，会「吞掉异常」，不利于排查 JWT 格式错误或密钥问题。建议：记录日志并区分「过期/签名错误」与「无效 token」；对外仍返回 null/false，但要有 trace。
- **其他 Service**：若存在多表写操作（如预算与记录联动），建议在对应方法上显式加 `@Transactional(rollbackFor = Exception.class)`，避免只回滚 RuntimeException。

**涉及文件：**
- `JwtUtil.java`（parseToken 内 catch 处打日志）
- 各 Service 实现类（按需补事务注解）

---

### 3. 日志（对应规范：日志规约）

**问题：**
- `application.yml` 中 `com.accounting: debug` 在生产环境会产生大量 SQL/日志，建议通过 profile 区分：生产使用 `info` 或 `warn`。
- 异常日志：`GlobalExceptionHandler` 中 `handleException` 已打印堆栈，可保留；业务异常 `BusinessException` 仅 `log.warn` 消息，可考虑在开发环境增加更多上下文（如 userId、请求参数脱敏）。

**建议：**
- 使用占位符：`log.warn("业务异常: {}", e.getMessage())` 已符合规范。
- 生产环境关闭 MyBatis SQL 打印：将 `log-impl` 改为 `org.apache.ibatis.logging.slf4j.Slf4jImpl`，并通过 `logging.level` 控制是否输出 SQL。

**涉及文件：**
- `application.yml`（profile 分离、日志级别与 MyBatis log-impl）
- `GlobalExceptionHandler.java`（保持占位符，可增加可选上下文）

---

### 4. 安全（对应规范：安全规约）

**严重问题：**
- **敏感信息硬编码**：`application.yml` 中数据库密码、微信 `appid`/`secret`、JWT `secret` 直接写在仓库中，违反「敏感信息不能出现在代码/配置仓库」。
- **CORS**：`CorsConfig` 使用 `addAllowedOriginPattern("*")` 且 `setAllowCredentials(true)`，任意来源可带凭证访问，存在 CSRF 与滥用风险。

**建议：**
- 敏感配置改为环境变量或配置中心，例如：
  - `spring.datasource.url/username/password` 从环境变量读取。
  - `wechat.appid`、`wechat.secret`、`jwt.secret` 从环境变量读取。
- CORS：生产环境限定具体 `allowedOrigins`（如小程序域名、H5 前端域名），避免 `*` + credentials。

**涉及文件：**
- `application.yml`（敏感项改为 `${ENV_VAR:default}`）
- `CorsConfig.java`（按环境配置允许的 origin）

---

### 5. 数据库与 SQL（对应规范：MySQL、索引、ORM）

**已做得好的：**
- MyBatis 使用 `#{}`，无 SQL 拼接，防注入。
- 表有逻辑删除、索引设计（如 `user_id`、`record_date` 等）合理。

**可优化：**
- **RecordMapper.xml**：`DATE_FORMAT(r.record_date, '%Y-%m') = #{yearMonth}` 会导致对 `record_date` 做函数运算，无法走索引。若数据量大，可考虑：
  - 使用范围条件：`r.record_date >= #{yearMonth}-01 AND r.record_date < #{yearMonthNext}`（需在 Java 中算出下月首日）。
- **实体**：`Budget` 中 `year_month` 已用 `@TableField("`year_month`")` 规避保留字，其他表若有保留字也建议加反引号或统一命名。
- **字符集**：建议库与表显式 `utf8mb4`，连接串中可指定 `connectionCollation=utf8mb4_unicode_ci`（与 init.sql 一致）。

**涉及文件：**
- `RecordMapper.xml`（月维度查询改为范围条件）
- `application.yml`（url 中 charset/collation 如有需要可统一）

---

### 6. 控制语句与可读性（对应规范：控制语句、OOP）

**问题：**
- `RecordServiceImpl` 中多处 `if (record.getType() == 2)`、`type == 1 ? totalExpense : totalIncome`，可读性差且易错。

**建议：**
- 用枚举或常量替代后，改为 `RecordTypeEnum.INCOME.getCode()`、`RecordTypeEnum.isIncome(record.getType())` 等，减少魔法数字。
- 金额汇总可抽成私有方法，如 `sumByType(List<RecordVO> list, RecordTypeEnum type)`，便于单测和复用。

**涉及文件：**
- `RecordServiceImpl.java`

---

### 7. 参数校验（对应规范：接口与参数）

**问题：**
- `RecordDTO` 中 `type` 仅 `@NotNull`，未限制必须为 1 或 2；`recordDate` 未校验格式（yyyy-MM-dd）。

**建议：**
- `type`：使用 `@Min(1) @Max(2)` 或自定义校验（如 `@RecordType`）。
- `recordDate`：`@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")` 或使用 `@JsonFormat` + `LocalDate` 在 DTO 中直接接收 LocalDate。

**涉及文件：**
- `RecordDTO.java`
- 其他 DTO（如 `BudgetDTO` 的 `yearMonth` 格式校验）

---

### 8. 接口与分层（对应规范：分层、命名）

**已做得好的：**
- Controller 只做参数与调用 Service，统一返回 `Result<T>`。
- Service 与 Mapper 分层清晰。

**可优化：**
- Controller 中 `HttpServletRequest request` 仅用于取 `userId`，可考虑使用 ThreadLocal、拦截器注入的「当前用户」对象或自定义注解 `@CurrentUserId`，减少 request 在方法间传递。
- 接口命名：REST 风格已较好，保持「名词复数 + 动词」即可。

---

## 二、前端优化建议

### 1. 常量与魔法值

**问题：**
- 页面与 API 中直接使用 `type === 1`、`type === 2`，以及多处重复的文案、路由路径。

**建议：**
- 新建 `constants/index.js`（或按模块拆分），例如：
  - `RECORD_TYPE_EXPENSE = 1`、`RECORD_TYPE_INCOME = 2`
  - `PAGE_PATH_ADD = '/pages/add/add'`
- 统一从常量引用，便于维护和避免拼写错误。

**涉及文件：**
- `pages/index/index.vue`、`pages/add/add.vue`、`pages/statistics/statistics.vue` 等
- 新建 `constants/index.js`

---

### 2. 请求与环境配置

**问题：**
- `utils/request.js` 与 `App.vue` 中 `BASE_URL` / `baseUrl` 写死为 `http://localhost:8080/api`，不利于多环境构建。

**建议：**
- 使用环境变量或 uni-app 的 `process.env` / 配置文件（如 `config/env.js`），按 `development` / `production` 切换 baseUrl。
- 小程序正式版请求域名必须在后台配置 HTTPS，baseUrl 应为完整后端域名。

**涉及文件：**
- `utils/request.js`
- `App.vue`
- 新建 `config/env.js` 或在 `manifest.json` 中通过不同 build 模式注入

---

### 3. 错误处理与用户提示

**问题：**
- 多处 `catch` 仅 `console.error`，用户只依赖 `request.js` 里 `uni.showToast` 的「请求失败」或后端 message，部分场景（如网络超时、解析失败）可再细化提示。
- 编辑记录失败后，当前表单未保留，用户可能误以为未提交成功。

**建议：**
- 在 `request.js` 的 `fail` 或 `success` 中根据 `res.statusCode`、`res.data.code` 做分层提示（如 401 跳转登录、5xx 提示稍后重试）。
- 重要操作（如保存、删除）在 catch 中统一 `uni.showToast` 一次，避免静默失败。

**涉及文件：**
- `utils/request.js`
- 各页面的 `catch` 块（至少保证一次用户可见提示）

---

### 4. 编辑记录功能缺失（功能缺陷）

**问题：**
- 从首页长按进入「编辑」时，仅通过 URL 传入 `id` 和 `edit=1`，**未根据 id 拉取原记录并回填表单**。用户看到的是空白或默认值，点击保存会用当前表单覆盖原记录，导致数据错误或误删信息。

**建议：**
- 后端提供「单条记录详情」接口，例如 `GET /record/{id}`，校验归属当前用户后返回记录（含分类、金额、日期、备注等）。
- 前端在 `onLoad` 中若 `options.edit === '1' && options.id`，先调用该接口，将返回数据写入 `amount`、`remark`、`recordDate`、`recordTime`、`type`、`selectedCategory`，再渲染页面。

**涉及文件：**
- 后端：`RecordController.java`、`RecordService`、`RecordServiceImpl.java`（新增 getById 且校验 userId）
- 前端：`api/index.js`（新增 getRecordDetail）、`pages/add/add.vue`（onLoad 时拉取并回填）

---

### 5. 代码复用与可维护性

**问题：**
- 多个页面重复「状态栏占位」「格式化金额」「月份切换」等逻辑。

**建议：**
- 状态栏高度：可在 `App.vue` 的 `onLaunch` 中写入 `getApp().globalData.statusBarHeight`，各页统一读取。
- 格式化金额、日期等已集中在 `utils/util.js`，可继续保持；若存在重复计算（如月度汇总），可考虑抽成 composable 或 mixin（在 Vue2 下可用 mixin）。

**涉及文件：**
- `App.vue`、各页面

---

### 6. 安全与存储

**问题：**
- Token 与用户信息存于 `uni.getStorageSync`，若小程序被反编译或运行在不安全环境，存在泄露风险（属通用问题，仅做规范建议）。

**建议：**
- 不在前端存储敏感字段（如后端不应把 openId 等敏感信息下发给前端长期保存，若已下发则前端避免再写入日志）。
- 请求失败时 401 清除 token 与 userInfo 的逻辑已存在，可保持。

**涉及文件：**
- `utils/request.js`、`App.vue`

---

## 三、优化项优先级汇总

| 优先级 | 类型     | 优化项                         | 说明                         |
|--------|----------|--------------------------------|------------------------------|
| P0     | 安全     | 敏感配置脱敏/环境变量          | 数据库、微信、JWT 不落库     |
| P0     | 功能     | 编辑记录回填                    | 避免误覆盖/误删数据          |
| P1     | 规范     | 魔法值改为常量/枚举             | 类型、默认账本等             |
| P1     | 安全     | CORS 按环境限制 origin          | 生产禁止 * + credentials     |
| P1     | 配置     | 前端 baseUrl 按环境配置         | 支持多环境发布               |
| P2     | 日志     | 生产关闭 debug/SQL 日志         | 性能与安全                   |
| P2     | 异常     | JWT 解析失败打日志              | 便于排查                     |
| P2     | 校验     | RecordDTO type/date 格式校验    | 防止非法参数                 |
| P2     | 性能     | 月账单 SQL 避免 DATE_FORMAT     | 可走索引                     |
| P3     | 可读性   | Controller 注入当前用户         | 减少 request 传递             |
| P3     | 前端     | 常量与错误提示统一              | 维护性与体验                 |

---

## 四、小结

- **后端**：整体分层和 MyBatis 使用符合规范；主要改进点集中在**消除魔法值**、**敏感配置脱敏**、**CORS/事务/日志/参数校验**以及**编辑记录所需单条查询接口**。
- **前端**：功能与接口封装较清晰；需要**补全编辑回填**、**环境化 baseUrl**、**常量与错误提示统一**，并与后端一起完成**敏感信息与 CORS** 的安全加固。

按上表 P0 → P1 → P2 的顺序落地，即可在符合阿里巴巴编码规范的前提下，显著提升安全性、可维护性和功能正确性。
