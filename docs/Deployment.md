# 微信记账小程序 - 部署与运行指南

本文档将指导您如何部署和运行“轻记账”小程序的后端服务和前端应用。

## 1. 环境要求

在开始之前，请确保您的开发环境中已安装以下软件：

| 软件 | 版本 | 用途 |
| :--- | :--- | :--- |
| JDK | 1.8+ | Java 开发环境 |
| Maven | 3.x+ | Java 项目构建与依赖管理 |
| MySQL | 8.0+ | 数据库服务 |
| Node.js | 16.x+ | 前端运行环境 |
| HBuilderX | 最新版 | uni-app 开发、调试和打包工具 |
| IntelliJ IDEA | 推荐 | Spring Boot 后端开发工具 |

## 2. 后端部署 (accounting-backend)

### 步骤 1: 数据库初始化

1.  确保您的 MySQL 服务正在运行。
2.  使用 MySQL 客户端（如 `mysql` 命令行、Navicat 等）连接到数据库。
3.  执行项目根目录 `sql/init.sql` 脚本。这将创建 `accounting_app` 数据库、所有必需的表，并插入初始的分类数据。

    ```bash
    mysql -u root -p < /path/to/accounting-app/sql/init.sql
    ```

### 步骤 2: 修改配置文件

1.  打开后端项目 `backend/src/main/resources/application.yml`。
2.  根据您的环境修改 `spring.datasource` 部分的数据库连接信息，特别是 `username` 和 `password`。

    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/accounting_app?...
        username: your_mysql_username
        password: your_mysql_password
    ```

3.  修改 `wechat` 部分的微信小程序配置。您需要填入在微信公众平台申请的小程序的 `appid` 和 `secret`。

    ```yaml
    wechat:
      appid: your-wechat-appid
      secret: your-wechat-secret
    ```

### 步骤 3: 运行后端服务

您可以通过以下两种方式之一来运行后端服务：

**方式一：使用 Maven 命令行**

在 `backend` 目录下，执行以下命令：

```bash
cd /path/to/accounting-app/backend
mvn spring-boot:run
```

**方式二：使用 IntelliJ IDEA**

1.  使用 IntelliJ IDEA 打开 `backend` 目录作为项目。
2.  等待 Maven 自动下载所有依赖。
3.  找到 `src/main/java/com/accounting/AccountingApplication.java` 文件。
4.  右键点击该文件，选择 “Run 'AccountingApplication'”。

服务启动成功后，您将在控制台看到类似 `Tomcat started on port(s): 8080 (http)` 的日志。API 服务将在 `http://localhost:8080/api` 上可用。

## 3. 前端部署 (accounting-frontend)

### 步骤 1: 安装依赖

由于本项目未使用外部 npm 依赖，此步骤可以跳过。如果将来添加了依赖，可以在 `frontend` 目录下执行 `npm install` 或 `pnpm install`。

### 步骤 2: 导入 HBuilderX

1.  打开 HBuilderX 开发工具。
2.  点击菜单栏 “文件” -> “导入” -> “从本地目录导入”。
3.  选择项目根目录下的 `frontend` 文件夹。

### 步骤 3: 修改配置

1.  在 HBuilderX 中，打开 `manifest.json` 文件。
2.  在 “微信小程序配置” 中，填入您自己的微信小程序 `AppID`。
3.  打开 `utils/request.js` 文件，确认 `BASE_URL` 是否与您的后端服务地址匹配。如果后端部署在远程服务器，请修改为相应的公网 IP 和端口。

    ```javascript
    const BASE_URL = 'http://your_backend_host:8080/api';
    ```

### 步骤 4: 运行到微信开发者工具

1.  在 HBuilderX 中，点击菜单栏 “运行” -> “运行到小程序模拟器” -> “微信开发者工具”。
2.  HBuilderX 将自动编译项目并启动微信开发者工具加载小程序代码。
3.  在微信开发者工具中，您可能需要设置 “不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书” 以便在本地进行调试。

现在，您应该可以在微信开发者工具中看到并操作“轻记账”小程序了。

## 4. H5 模式预览 (可选)

本项目也支持在浏览器中以 H5 模式预览。

1.  在 HBuilderX 中，点击菜单栏 “运行” -> “运行到浏览器” -> 选择一个浏览器。
2.  HBuilderX 会启动一个本地开发服务器，并在浏览器中打开页面。
3.  H5 模式会自动配置代理，将 `/api` 请求转发到 `http://localhost:8080`，因此可以直接与本地后端联调。
