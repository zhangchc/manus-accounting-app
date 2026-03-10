# 微信记账小程序 - 轻记账

## 1. 项目概述

本项目是一个功能完整的微信记账小程序，旨在为用户提供一个简约、清爽、温馨的记账体验。用户可以通过该小程序轻松记录每日的收入和支出，管理个人财务，并通过直观的图表了解自己的消费习惯。

### 主要功能

- **微信授权登录**：支持微信一键登录，自动创建用户账户。开发模式支持免微信授权登录。
- **多账本管理**：支持创建和管理多个账本，如"日常账本"、"旅游账本"等。
- **快捷记账**：提供便捷的记账界面，用户可以快速输入金额、选择分类、添加备注。
- **收支分类**：系统预设 21 个支出分类和 8 个收入分类，并支持用户自定义分类。
- **月度账单**：按月展示详细的收支流水，按日分组，清晰了解每一笔钱的去向。
- **统计分析**：提供多维度的数据统计，包括分类排行（百分比）、每日趋势图、日均消费等。
- **预算管理**：支持设置月度总预算，实时跟踪预算使用情况（正常/警告/超支三级颜色）。
- **年度汇总**：展示年度总收入、总支出和结余，回顾一年来的财务状况。

### 产品界面风格

遵循"简约、清爽、温馨"风格，UI 设计采用柔和的色彩搭配、清晰的布局和友好的交互。

- **主色调**：柔和蓝色 `#7C9FF5`，营造宁静、专业的氛围
- **辅助色**：温馨粉色 `#F5A0B5`、暖橙 `#F5C07C`、清新绿 `#8DD5B0`、淡紫 `#B8A0F5`
- **支出颜色**：柔和红色 `#F5707A`
- **收入颜色**：清新绿色 `#5CC9A7`
- **背景色**：淡雅灰蓝 `#F8F9FE`
- **布局**：卡片式设计，大圆角（24rpx），轻柔蓝色阴影
- **图标**：Emoji 图标，生动活泼

## 2. 技术栈

| 分类 | 技术 | 版本/说明 |
| :--- | :--- | :--- |
| **前端** | uni-app | 基于 Vue.js 的跨平台开发框架 |
| | Vue.js | 2.x（兼容 Vue 3 条件编译） |
| | SCSS | CSS 预处理器 |
| **后端** | Spring Boot | 2.7.18 |
| | MyBatis-Plus | 3.5.5，简化数据库操作 |
| | MySQL | 8.0，关系型数据库 |
| | Druid | 1.2.21，数据库连接池 |
| | JJWT | 0.9.1，JWT 令牌生成与校验 |
| | Hutool | 5.8.25，Java 工具库 |
| | Lombok | 简化实体类代码 |
| **开发环境** | JDK | 1.8（必须） |
| | Maven | 3.x，项目管理工具 |
| | HBuilderX | uni-app 开发工具 |
| | IntelliJ IDEA | Spring Boot 开发工具（推荐） |

## 3. 项目结构

```
accounting-app/
├── sql/                          # 数据库脚本
│   └── init.sql                  # 建库建表 + 预设分类数据
├── backend/                      # Spring Boot 后端
│   ├── pom.xml                   # Maven 依赖配置
│   └── src/main/
│       ├── java/com/accounting/
│       │   ├── AccountingApplication.java    # 启动类
│       │   ├── common/                       # 通用类
│       │   │   ├── Result.java               # 统一响应封装
│       │   │   ├── BusinessException.java    # 业务异常
│       │   │   └── GlobalExceptionHandler.java # 全局异常处理
│       │   ├── config/                       # 配置类
│       │   │   ├── MybatisPlusConfig.java    # MP 分页 + 自动填充
│       │   │   ├── CorsConfig.java           # 跨域配置
│       │   │   └── WebConfig.java            # JWT 认证拦截器
│       │   ├── controller/                   # 控制器（5个）
│       │   │   ├── UserController.java       # 用户：登录、信息
│       │   │   ├── BookController.java       # 账本：CRUD
│       │   │   ├── CategoryController.java   # 分类：列表、创建、删除
│       │   │   ├── RecordController.java     # 记录：CRUD + 统计
│       │   │   └── BudgetController.java     # 预算：设置、查询
│       │   ├── dto/                          # 请求数据传输对象
│       │   │   ├── LoginDTO.java
│       │   │   ├── RecordDTO.java
│       │   │   └── BudgetDTO.java
│       │   ├── entity/                       # 数据库实体类
│       │   │   ├── User.java
│       │   │   ├── Book.java
│       │   │   ├── Category.java
│       │   │   ├── Record.java
│       │   │   └── Budget.java
│       │   ├── mapper/                       # MyBatis Mapper 接口
│       │   │   ├── UserMapper.java
│       │   │   ├── BookMapper.java
│       │   │   ├── CategoryMapper.java
│       │   │   ├── RecordMapper.java
│       │   │   └── BudgetMapper.java
│       │   ├── service/                      # Service 接口 + 实现
│       │   │   ├── UserService.java
│       │   │   ├── BookService.java
│       │   │   ├── CategoryService.java
│       │   │   ├── RecordService.java
│       │   │   ├── BudgetService.java
│       │   │   └── impl/                     # 5 个实现类
│       │   ├── utils/
│       │   │   └── JwtUtil.java              # JWT 工具类
│       │   └── vo/                           # 响应视图对象
│       │       ├── RecordVO.java
│       │       ├── DailyBillVO.java
│       │       ├── MonthBillVO.java
│       │       └── StatisticsVO.java
│       └── resources/
│           ├── application.yml               # 应用配置
│           └── mapper/
│               └── RecordMapper.xml          # 自定义 SQL 映射
├── frontend/                     # uni-app 前端
│   ├── manifest.json             # 应用配置（含 H5 代理）
│   ├── pages.json                # 页面路由 + tabBar
│   ├── App.vue                   # 应用入口（含自动登录）
│   ├── main.js                   # Vue 入口（兼容 Vue2/3）
│   ├── uni.scss                  # 全局 SCSS 变量
│   ├── index.html                # H5 入口
│   ├── package.json
│   ├── api/
│   │   └── index.js              # API 接口封装（21 个接口）
│   ├── utils/
│   │   ├── request.js            # 网络请求封装（Token 拦截）
│   │   └── util.js               # 工具函数（日期/金额格式化）
│   ├── pages/
│   │   ├── index/index.vue       # 首页（月度总览 + 今日账单）
│   │   ├── add/add.vue           # 记账页（数字键盘 + 分类选择）
│   │   ├── statistics/statistics.vue  # 统计页（分类排行 + 趋势图）
│   │   ├── mine/mine.vue         # 我的页（年度汇总 + 功能菜单）
│   │   ├── bill/bill.vue         # 月度账单页（按日分组）
│   │   └── budget/budget.vue     # 预算管理页（进度条）
│   └── static/icons/             # tabBar 图标（8 个 PNG）
└── docs/
    ├── API.md                    # API 接口文档
    └── Deployment.md             # 部署指南
```

## 4. 数据库设计

数据库共包含 5 张核心业务表：

| 表名 | 说明 | 关键字段 |
| :--- | :--- | :--- |
| `t_user` | 用户表 | open_id, nick_name, monthly_budget |
| `t_book` | 账本表 | user_id, name, is_default |
| `t_category` | 分类表 | user_id(0=系统预设), name, icon, type(1支出/2收入) |
| `t_record` | 记账记录表 | user_id, book_id, category_id, type, amount, record_date |
| `t_budget` | 预算表 | user_id, category_id(NULL=总预算), amount, year_month |

所有表均支持逻辑删除（`deleted` 字段）和自动填充时间（`create_time`, `update_time`）。

预设分类数据：
- **支出**（21 个）：餐饮、交通、购物、日用、水果、零食、运动、娱乐、通讯、服饰、美容、住房、居家、孩子、长辈、社交、旅行、宠物、医疗、学习、其他
- **收入**（8 个）：工资、奖金、兼职、理财、红包、转账、退款、其他

## 5. 快速开始

### 5.1 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 5.2 配置后端

编辑 `backend/src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    username: your_username    # 你的 MySQL 用户名
    password: your_password    # 你的 MySQL 密码

wechat:
  appid: your-real-appid       # 微信小程序 AppID（开发调试可不改）
  secret: your-real-secret     # 微信小程序 Secret（开发调试可不改）
```

### 5.3 启动后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/accounting-backend-1.0.0.jar
```

后端启动后访问：`http://localhost:8080/api`

### 5.4 运行前端

**方式一：微信小程序模式（推荐）**
1. 安装 [HBuilderX](https://www.dcloud.io/hbuilderx.html)
2. 用 HBuilderX 打开 `frontend` 目录
3. 运行 → 运行到小程序模拟器 → 微信开发者工具

**方式二：H5 调试模式**
1. 用 HBuilderX 打开 `frontend` 目录
2. 运行 → 运行到浏览器
3. 访问 `http://localhost:8081`（已配置 `/api` 代理到后端）

### 5.5 开发调试

项目内置开发模式登录（`POST /api/user/login`），`App.vue` 启动时会自动调用，无需配置微信小程序即可进行本地调试。

## 6. API 接口概览

| 模块 | 接口数 | 说明 |
| :--- | :--- | :--- |
| 用户 | 4 | 微信登录、开发登录、获取/更新用户信息 |
| 账本 | 5 | CRUD + 获取默认账本 |
| 分类 | 3 | 列表查询、创建、删除自定义分类 |
| 记录 | 7 | CRUD + 月账单 + 统计 + 年度汇总 + 今日支出 |
| 预算 | 2 | 设置预算、查询月度预算 |

详细接口文档请参阅 [docs/API.md](./docs/API.md)。

## 7. 注意事项

1. **JDK 版本**：必须使用 JDK 1.8，不兼容更高版本（Spring Boot 2.7 + javax 包）
2. **MySQL 版本**：推荐 MySQL 8.0，已处理时区（`serverTimezone=Asia/Shanghai`）和字符集（`utf8mb4`）
3. **微信小程序上线**：需要在微信公众平台配置服务器域名白名单
4. **开发调试**：使用 `/user/login` 接口可跳过微信授权直接登录
5. **数据安全**：所有业务接口均需 JWT Token 认证（登录接口除外）
6. **H5 模式**：`manifest.json` 中已配置 `/api` 代理到 `http://localhost:8080`

## 8. 部署文档

详细的部署步骤请参见 [docs/Deployment.md](./docs/Deployment.md)。
