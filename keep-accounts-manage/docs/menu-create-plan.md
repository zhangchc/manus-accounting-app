# 子级功能（createMenu）技术方案

## 改动范围

只动新增逻辑（`handleSave` 中 `!editItem` 的分支），编辑仍留空。

## 前端改动（1 个文件）

### 1. 新增 API — `src/api/menu.js`

```js
export function createMenu(data) {
  return request.post('/manage/menu', data)
}
```

### 2. 弹窗改造 — `views/system/menu/index.vue`

#### 2.1 新增状态变量

```js
const lockType = ref(false)    // true=禁用类型切换
const lockParent = ref(false)  // true=禁用上级菜单选择
```

#### 2.2 三个入口分别控制锁定状态

```js
// 顶部"新增菜单"：自由选择
function openAdd() {
  editItem.value = null
  lockType.value = false
  lockParent.value = false
  menuType.value = 'dir'
  formStatus.value = true
  Object.assign(form, { parentId: '', name: '', icon: '', path: '', component: '', permission: '', sort: 1 })
  showModal.value = true
}

// "+子级"：类型和上级均锁定
function openAddChild(parent) {
  editItem.value = null
  lockType.value = true          // ★ 类型不可切换
  lockParent.value = true        // ★ 上级不可切换
  menuType.value = parent.type === 'dir' ? 'menu' : 'btn'
  formStatus.value = true
  Object.assign(form, { parentId: parent.id, name: '', icon: '', path: '', component: '', permission: '', sort: 1 })
  showModal.value = true
}

// "编辑"：类型可切，上级可切（后续实现，暂留原样）
```

#### 2.3 上级菜单选择改造

**现状问题**：`<select>` 没绑定 v-model，option 硬编码两个值

**改为**：

```html
<select v-model="form.parentId" :disabled="lockParent" :style="{...fieldStyle, cursor: lockParent ? 'not-allowed' : 'pointer'}">
  <option value="">顶级菜单（无上级）</option>
  <option v-for="p in parentOptions" :key="p.id" :value="p.id">{{ p.name }}</option>
</select>
```

`parentOptions` 从 `mockMenus` 树中提取所有非 btn 节点（dir 和 menu 都能做父级）：

```js
const parentOptions = computed(() => {
  const result = []
  function collect(items) {
    for (const item of items) {
      if (item.type !== 'btn') result.push({ id: item.id, name: item.name })
      if (item.children) collect(item.children)
    }
  }
  collect(mockMenus.value)
  return result
})
```

#### 2.4 菜单类型选择器改造

```html
<!-- 锁定态时整个按钮组置灰不可点击 -->
<div style="display:flex;border:1px solid #E2E8F0;border-radius:10px;overflow:hidden;height:36px;"
     :style="{opacity: lockType ? 0.5 : 1}">
  <button v-for="t in ['dir','menu','btn']" :key="t"
    :disabled="lockType"
    @click="!lockType && (menuType = t)"
    :style="{...}">
    {{ typeConfig[t].label }}
  </button>
</div>
```

### 3. handleSave 实现

```js
async function handleSave() {
  if (editItem.value) {
    // 编辑暂不实现
    showModal.value = false
    return
  }
  try {
    await createMenu({
      parentId: form.parentId || 0,
      name: form.name,
      icon: form.icon,
      type: menuType.value,
      path: form.path,
      component: form.component,
      permission: form.permission,
      sort: form.sort,
      status: formStatus.value,
    })
    showModal.value = false
    // 重新加载菜单树
    const data = await getMenuTree()
    if (data && data.length) mockMenus.value = data
  } catch (e) {
    // 错误由全局拦截器处理
  }
}
```

## 后端改动（3 个文件）

### 1. 新增 DTO — `dto/MenuCreateDTO.java`

```java
@Data
public class MenuCreateDTO {
    private Long parentId;
    @NotBlank private String name;
    private String icon;
    @NotBlank private String type;
    private String path;
    private String component;
    private String permission;
    private Integer sort;
    private Boolean status;
}
```

### 2. Service 新增方法 — `MenuService.java` + `MenuServiceImpl.java`

```java
void create(MenuCreateDTO dto);
```

实现逻辑：
1. 校验 `parentId`：如果非 0，查父菜单是否存在
2. 校验层级规则：
   - `parentId == 0` → type 只能是 `dir` 或 `menu`
   - 父菜单 type == `dir` → 子菜单 type 必须是 `menu`
   - 父菜单 type == `menu` → 子菜单 type 必须是 `btn`
3. SysMenu 赋值，`createdUser` 从 request attribute 取 userId
4. `mapper.insert(menu)`

### 3. Controller 新增端点 — `MenuController.java`

```java
@PostMapping
public Result<Void> create(@Valid @RequestBody MenuCreateDTO dto) {
    menuService.create(dto);
    return Result.success();
}
```

## 入口行为对照表

| 入口 | 类型选择器 | 上级菜单 | handleSave |
|------|------|------|------|
| 顶部"新增菜单" | 自由切换 | 可选任意非btn节点或顶级 | POST 新增 |
| "+子级" | **锁定禁用** | **锁定当前行** | POST 新增 |
| "编辑" | 可切换（暂留空） | 可切换（暂留空） | 暂不实现 |

## 层级约束规则

| parentId | 父类型 | 允许的子类型 | 不允许 |
|------|------|------|------|
| 0（顶级） | — | dir, menu | btn |
| 有值 | dir | menu | dir, btn |
| 有值 | menu | btn | dir, menu |
| 有值 | btn | —（"+子级"按钮不出现） | 全部 |

## 文件变更清单

| 文件 | 操作 |
|------|------|
| `keep-accounts-manage/.../dto/MenuCreateDTO.java` | 新增 |
| `keep-accounts-manage/.../service/MenuService.java` | 修改：加 create 方法 |
| `keep-accounts-manage/.../service/impl/MenuServiceImpl.java` | 修改：实现 create |
| `keep-accounts-manage/.../controller/MenuController.java` | 修改：加 POST 端点 |
| `keep-accounts-pc/src/api/menu.js` | 修改：加 createMenu |
| `keep-accounts-pc/src/views/system/menu/index.vue` | 修改：弹窗逻辑 |
