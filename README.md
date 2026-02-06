# 门店库存管理系统

门店库存管理系统（后端 Spring Boot + 前端 Vue3/Vite）。

## 目录结构

- `management/`：后端 Spring Boot 工程（Maven）
- `store_inventory_management_system_web/`：前端工程（Vue3 + Vite）
- `第四章_系统设计.md`：论文章节内容

## 环境要求

- JDK 17
- Maven 3.x
- Node.js >= 18
- MySQL 8.x
- Redis 6.x

## 本地运行

### 1) 启动后端

在仓库根目录执行：

```bash
mvn -f management/pom.xml spring-boot:run
```

服务默认启动信息（以 `management/src/main/resources/application.yaml` 为准）：

- 端口：`8000`
- `context-path`：`/api/system`
- Swagger UI：`http://localhost:8000/api/system/swagger-ui.html`

### 2) 初始化数据库

SQL 建表脚本位于 `management/src/main/java/com/qzh/backend/sql/`。

## 配置说明

运行前请根据你的本地环境在 `management/src/main/resources/application.yaml` 配置：

- `spring.datasource.*`（MySQL 连接）
- `spring.data.redis.*`（Redis 连接）
- 其他第三方服务相关配置（如有）

### 3) 启动前端

在仓库根目录执行：

```bash
cd store_inventory_management_system_web
npm install
npm run dev
```

前端开发代理已配置为将 `/api/system` 转发到 `http://127.0.0.1:8000`。

## 构建

### 前端构建

```bash
cd store_inventory_management_system_web
npm run typecheck
npm run build
```

### 后端打包

```bash
mvn -f management/pom.xml -DskipTests package
```

