# 菜单管理 - 查询功能技术方案

## 范围

**仅实现菜单列表查询（GET 接口），新增 / 编辑 / 删除维持前端 mock 假数据不变。**

## 整体流程

```
浏览器加载菜单页 → onMounted 调 GET /manage/menu/list
  → MenuController → MenuService.queryTree()
    → MenuMapper.selectList() 查全表
    → Service 层平铺转树形
  → 返回 [{ id, name, ..., children: [...] }]
→ 前端替换 mockMenus 初始值，后续增删改仍操作本地数据
```

## 后端实现

### 1. 新增文件清单

```
keep-accounts-manage/src/main/java/com/accounting/
├── entity/SysMenu.java          # 实体，映射 sys_menu 表
├── mapper/SysMenuMapper.java    # Mapper 接口
├── vo/MenuVO.java               # 响应 VO（含 children + parentId）
├── service/MenuService.java     # 业务接口
├── service/impl/MenuServiceImpl.java  # 业务实现（平铺→树形）
└── controller/MenuController.java     # GET /manage/menu/list
```

### 2. SysMenu 实体

```java
@TableName("sys_menu")
@Data
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private String icon;
    private String type;       // dir / menu / btn
    private String path;
    private String component;
    private String permission;
    private Integer sort;
    private Integer status;    // 1启用 0禁用
    // 审计字段
    private Long createdUser;
    private Long updatedUser;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
```

### 3. MenuVO 响应体

与前端 mockMenus 字段**完全对齐**，额外增加 `parentId` 用于编辑表单回显：

```java
@Data
public class MenuVO {
    private Long id;
    private Long parentId;       // 额外字段，编辑表单回显用
    private String name;
    private String icon;
    private String type;
    private String path;
    private String component;
    private String permission;
    private Integer sort;
    private Boolean status;      // int → boolean
    private List<MenuVO> children;
}
```

字段映射关系：

| 前端 mock 字段 | MenuVO 字段 | 来源 |
|------|------|------|
| id | id | sys_menu.id |
| name | name | sys_menu.name |
| icon | icon | sys_menu.icon |
| type | type | sys_menu.type |
| path | path | sys_menu.path |
| component | component | sys_menu.component |
| permission | permission | sys_menu.permission |
| sort | sort | sys_menu.sort |
| status | status | sys_menu.status (1→true, 0→false) |
| children | children | Service 层组装 |
| parentId | parentId | sys_menu.parent_id，编辑表单回显 |

### 4. Mapper

```java
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}
```

无需自定义 SQL，使用 MyBatis-Plus 自带 `selectList`，按 `sort` 升序查全表即可。

### 5. Service

```java
public interface MenuService {
    List<MenuVO> queryTree();
}
```

`MenuServiceImpl.queryTree()` 逻辑：
1. `mapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort))`
2. 过滤出 parentId == 0 的作为根节点
3. 递归挂接 children
4. SysMenu → MenuVO 转换，status int→boolean

树形构建使用 stream + filter，时间复杂度 O(n)。菜单数据量小（< 100 条），无需优化。

### 6. Controller

```
GET /manage/menu/list
```

```java
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/list")
    public Result<List<MenuVO>> list() {
        return Result.success(menuService.queryTree());
    }
}
```

## 前端改造（最小化）

### 1. 新增 `src/api/menu.js`

```js
import axios from 'axios'

export function getMenuTree() {
  return axios.get('/manage/menu/list').then(res => res.data.data)
}
```

### 2. Axios 实例配置

创建 `src/utils/request.js`：

```js
import axios from 'axios'

const request = axios.create({
  baseURL: '',          // Vite proxy 已配 /manage → localhost:9092
  timeout: 10000,
})

// 请求拦截器：注入 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器：统一解包
request.interceptors.response.use(
  res => res.data,
  err => Promise.reject(err)
)

export default request
```

### 3. Vite 代理配置

`vite.config.js` 新增 proxy：

```js
proxy: {
  '/manage': {
    target: 'http://localhost:9092',
    changeOrigin: true,
  }
}
```

### 4. 改造 `views/system/menu/index.vue`

**改动点**（仅 3 处，最小化）：

```js
// 改动 1：引入 API
import { getMenuTree } from '@/api/menu'
import { onMounted } from 'vue'

// 改动 2：mockMenus 初始化为空数组
const mockMenus = ref([])  // 原来是 [...mock数据]

// 改动 3：onMounted 拉取数据
onMounted(async () => {
  const data = await getMenuTree()
  if (data && data.length) mockMenus.value = data
})
```

**不改动的地方**：
- `expandAll()` / `collapseAll()` — 仍然遍历 `mockMenus.value`
- `flatRows` computed — 仍然 walk `mockMenus.value`
- `openAdd()` / `openAddChild()` / `openEdit()` — 表单逻辑不变
- `handleDelete()` / `confirmDelete()` — 仍操作本地 `mockMenus.value`
- `handleSave()` — 仍为空函数，不持久化

## 关键设计决策

### 为什么查全表而不是按需懒加载？

菜单数据量极小（15-50 条），全表查出后前端一次性构建树，比多次请求更简单，且前端 expand/collapse 本来就是全量树的前端过滤。

### 为什么 MenuVO 多带一个 parentId？

前端 `openEdit` 时读取 `item.parentId` 回显上级菜单 select。树形结构本身隐含父子关系，但编辑表单的 select 框需要一个明确的 parentId 值。mock 数据中顶级节点没有此字段（fallback 到 `''`），后端带上后无需修改前端表单逻辑。

### 为什么不一次性做增删改？

1. 增删改涉及权限校验、审计字段填充、缓存失效等，复杂度高
2. 先接查询，验证前端→后端→数据库整条链路通畅
3. 后续按模块逐个接，风险可控

## 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `keep-accounts-manage/.../entity/SysMenu.java` | 新增 | 菜单实体 |
| `keep-accounts-manage/.../mapper/SysMenuMapper.java` | 新增 | Mapper 接口 |
| `keep-accounts-manage/.../vo/MenuVO.java` | 新增 | 响应 VO |
| `keep-accounts-manage/.../service/MenuService.java` | 新增 | 业务接口 |
| `keep-accounts-manage/.../service/impl/MenuServiceImpl.java` | 新增 | 业务实现 |
| `keep-accounts-manage/.../controller/MenuController.java` | 新增 | 控制器 |
| `keep-accounts-pc/src/api/menu.js` | 新增 | 前端 API |
| `keep-accounts-pc/src/utils/request.js` | 新增 | Axios 实例 |
| `keep-accounts-pc/vite.config.js` | 修改 | 加 proxy |
| `keep-accounts-pc/src/views/system/menu/index.vue` | 修改 | 接后端数据 |
