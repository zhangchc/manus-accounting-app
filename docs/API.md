# 微信记账小程序 - API 接口文档

## 1. 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: JWT (JSON Web Token)
- **认证Header**: `Authorization: Bearer <token>`

所有需要认证的接口，都需要在请求头中携带登录时获取的 `token`。

## 2. 统一响应格式

所有接口均返回统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... } // 具体的业务数据
}
```

- **`code`**: 状态码。`200` 表示成功，`401` 表示未认证/认证失败，`500` 表示服务器内部错误或业务异常。
- **`message`**: 提示信息。
- **`data`**: 响应的业务数据，可能为 `null`、对象或数组。

---

## 3. 用户模块 (`/user`)

### 3.1 微信登录

- **URL**: `/user/wxLogin`
- **Method**: `POST`
- **认证**: 否
- **请求体 (JSON)**:

```json
{
  "code": "wx_login_code", // 微信登录凭证
  "nickName": "微信昵称",
  "avatarUrl": "头像URL",
  "gender": 1 // 0-未知 1-男 2-女
}
```

- **响应数据**:

```json
{
  "token": "jwt_token_string",
  "userInfo": { ... }, // 用户信息对象
  "isNewUser": true // 是否为新注册用户
}
```

### 3.2 开发模式登录

- **URL**: `/user/login`
- **Method**: `POST`
- **认证**: 否
- **说明**: 用于本地开发调试，`code` 字段会作为 `openId` 的一部分（`dev_` + `code`）。
- **请求体 (JSON)**:

```json
{
  "code": "test_user_001", // 任意字符串
  "nickName": "测试用户"
}
```

### 3.3 获取用户信息

- **URL**: `/user/info`
- **Method**: `GET`
- **认证**: 是
- **响应数据**: 用户对象 `User`。

### 3.4 更新用户信息

- **URL**: `/user/info`
- **Method**: `PUT`
- **认证**: 是
- **请求体 (JSON)**: 用户对象 `User` 的部分字段，如 `nickName`, `avatarUrl`, `monthlyBudget`。

---

## 4. 记账记录模块 (`/record`)

### 4.1 添加记账

- **URL**: `/record`
- **Method**: `POST`
- **认证**: 是
- **请求体 (JSON)**:

```json
{
  "bookId": 1, // 账本ID (可选)
  "categoryId": 101,
  "type": 1, // 1-支出 2-收入
  "amount": 25.50,
  "remark": "午餐",
  "recordDate": "2026-03-10",
  "recordTime": "12:30" // 可选
}
```

### 4.2 更新记账

- **URL**: `/record`
- **Method**: `PUT`
- **认证**: 是
- **请求体 (JSON)**: 同上，但必须包含 `id` 字段。

### 4.3 删除记账

- **URL**: `/record/{id}`
- **Method**: `DELETE`
- **认证**: 是
- **URL参数**: `id` - 记账记录ID。

### 4.4 获取月度账单

- **URL**: `/record/bill/month`
- **Method**: `GET`
- **认证**: 是
- **Query参数**:
  - `yearMonth`: `yyyy-MM` 格式，如 `2026-03`。
  - `bookId`: 账本ID (可选，不传则查询所有账本)。
- **响应数据**: `MonthBillVO` 对象。

### 4.5 获取统计数据

- **URL**: `/record/statistics`
- **Method**: `GET`
- **认证**: 是
- **Query参数**:
  - `yearMonth`: `yyyy-MM` 格式。
  - `type`: `1` (支出) 或 `2` (收入)。
- **响应数据**: `StatisticsVO` 对象。

### 4.6 获取年度汇总

- **URL**: `/record/summary/year`
- **Method**: `GET`
- **认证**: 是
- **Query参数**: `year` - `yyyy` 格式，如 `2026`。

### 4.7 获取今日支出

- **URL**: `/record/today/expense`
- **Method**: `GET`
- **认证**: 是

---

## 5. 分类模块 (`/category`)

### 5.1 获取分类列表

- **URL**: `/category/list`
- **Method**: `GET`
- **认证**: 是
- **Query参数**: `type` - `1` (支出) 或 `2` (收入)，可选，不传则返回所有类型。
- **说明**: 返回系统预设和用户自定义的分类列表。

### 5.2 创建自定义分类

- **URL**: `/category`
- **Method**: `POST`
- **认证**: 是
- **请求体 (JSON)**:

```json
{
  "name": "投资理财",
  "icon": "📈",
  "type": 2, // 1-支出 2-收入
  "sortOrder": 10
}
```

### 5.3 删除自定义分类

- **URL**: `/category/{id}`
- **Method**: `DELETE`
- **认证**: 是
- **URL参数**: `id` - 分类ID。
- **说明**: 只能删除用户自定义的分类。

---

## 6. 预算模块 (`/budget`)

### 6.1 设置预算

- **URL**: `/budget`
- **Method**: `POST`
- **认证**: 是
- **请求体 (JSON)**:

```json
{
  "categoryId": null, // 分类ID，为null表示总预算
  "amount": 3000.00,
  "yearMonth": "2026-03"
}
```

### 6.2 获取月度预算列表

- **URL**: `/budget/month`
- **Method**: `GET`
- **认证**: 是
- **Query参数**: `yearMonth` - `yyyy-MM` 格式。

---

## 7. 账本模块 (`/book`)

### 7.1 获取账本列表

- **URL**: `/book/list`
- **Method**: `GET`
- **认证**: 是

### 7.2 获取默认账本

- **URL**: `/book/default`
- **Method**: `GET`
- **认证**: 是

### 7.3 创建账本

- **URL**: `/book`
- **Method**: `POST`
- **认证**: 是
- **请求体 (JSON)**:

```json
{
  "name": "旅游账本",
  "icon": "✈️",
  "sortOrder": 1
}
```

### 7.4 更新账本

- **URL**: `/book`
- **Method**: `PUT`
- **认证**: 是
- **请求体 (JSON)**: 同上，但必须包含 `id` 字段。

### 7.5 删除账本

- **URL**: `/book/{id}`
- **Method**: `DELETE`
- **认证**: 是
- **URL参数**: `id` - 账本ID。
- **说明**: 默认账本不能删除。
