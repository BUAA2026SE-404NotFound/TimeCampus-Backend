# 时光航迹后端（Time Track Backend）

一个基于 Spring Boot 的多模块后端项目，为“时光航迹”微信小程序提供 RESTful API 服务。

## 项目定位

- 面向微信小程序用户端的后端能力：登录、地点信息、评论收藏、文件上传等
- 面向管理端 Web 的后端能力：用户管理、内容管理、运营统计等

## 分支策略

- `main`：稳定分支，只允许从 `release` 合并
- `release`：预发布分支，只允许从 `dev` 合并
- `dev`：日常集成分支，功能开发从此分支切出
- `feature/*`、`fix/*`、`chore/*`：功能/修复/维护分支，完成后合并回 `dev`

## 项目结构

- `timetrack-common`：公共能力（统一返回、全局异常、Web 过滤器等）
- `timetrack-pojo`：实体、DTO、VO 等数据模型
- `timetrack-server`：业务服务、控制器、Mapper、配置与启动类

目录示例：

- `timetrack-server/src/main/java/com/notfound/timetrackserver/controller`
- `timetrack-server/src/main/java/com/notfound/timetrackserver/service`
- `timetrack-server/src/main/java/com/notfound/timetrackserver/mapper`
- `timetrack-server/src/main/resources/mapper`

## 服务器运行环境

- JDK：**21**
- Maven：3.8
- Spring Boot：**3.3.11**
- MySQL：**8.0**
- Redis：**7.0**

## 配置文件说明

- `timetrack-server/src/main/resources/application.yaml`：通用配置
- `timetrack-server/src/main/resources/application-dev.yaml`：本地开发配置
- `timetrack-server/src/main/resources/application-prod.yaml`：生产模板

敏感信息（数据库密码、Redis 密码、服务器 IP/账号/密码、第三方 AK）使用 GitHub Secrets 或服务器环境变量管理。

## 数据库初始化（连接测试）

初始化脚本位置：

- `timetrack-server/src/main/resources/sql/test_db.sql`

脚本会创建：

- 数据库：`test_db`
- 表：`user`
- 一条测试数据（便于联通性验证）

## CI/CD 工作流

- `ci`：`.github/workflows/ci.yml`
  - 触发：任意分支 `push` / 任意分支 `pull_request`
  - 内容：`mvn clean test` + 服务模块打包 + 构建产物上传
- `cd`：`.github/workflows/cd.yml`
  - 触发：`main` 分支 `push`（以及手动触发）
  - 内容：构建并部署到云服务器
  - Java 运行环境：**JDK 21**

## 快速启动

1. 启动本机 MySQL 与 Redis-server
2. 执行数据库初始化脚本 `test_db.sql`
3. 按需修改 `application-dev.yaml` 的本地连接信息
4. 在项目根目录执行构建与测试
5. 启动服务模块

```shell
mvn clean test
mvn -pl timetrack-server -am spring-boot:run
```

## 接口文档

服务启动后访问：

- `http://localhost:8080/swagger-ui/index.html`

