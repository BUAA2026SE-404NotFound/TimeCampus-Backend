# TimeCampus Backend

TimeCampus Backend 是“时光航迹”的后端子模块，提供用户端、小程序、公开 Portal、Web 管理端、Agent HTTP API 和 MCP Server。当前 Maven 版本为 `0.3.0-beta`，基于 Java 21、Spring Boot 3.5.14、Spring AI 1.1.7、MyBatis、MySQL、Valkey/Redis、Qdrant 和腾讯地图 WebService。

## 模块结构

```text
timecampus-common   # 统一响应、错误码、业务异常、全局异常、请求 ID、Token 工具
timecampus-pojo     # DTO / Entity / VO / 常量 / 字段契约
timecampus-server   # Spring Boot 应用、Controller、Service、Mapper、MCP/RAG、配置
docs/               # 数据库、MCP、测试报告、发布说明
```

`timecampus-server` 的 Controller 按入口拆分：

```text
controller/
  admin/            # Web 管理端和管理端 Agent API
  publicapi/        # 无管理员 token 的 Portal 公开接口
  user/             # 小程序/用户端接口
```

## 本地启动

准备本地配置：

```powershell
Copy-Item timecampus-server\src\main\resources\application-dev-example.yaml `
  timecampus-server\src\main\resources\application-dev.yaml
```

填写 MySQL、Redis、微信、腾讯地图、Cap 等配置后运行：

```powershell
mvn test
mvn -pl timecampus-server -am spring-boot:run
```

默认地址：

```text
http://localhost:8080
http://localhost:8080/swagger-ui/index.html
```

根仓库也提供后端 + MCP 本地联调脚本：

```powershell
..\tools\start-backend-mcp.ps1
```

## API 分组

统一前缀为 `/api/v1`，统一响应格式为：

```json
{"code":0,"message":"ok","data":{}}
```

主要分组：

| 分组 | 路径 |
| --- | --- |
| 健康检查 | `/health`、`/actuator/health` |
| 用户登录 | `/auth/wechat/login` |
| 当前用户 | `/me`、`/me/review-results/**` |
| POI/内容 | `/pois`、`/contents`、`/timeline` |
| 收藏/UGC/评论 | `/favorites`、`/ugc`、`/comments` |
| 地图/媒体 | `/map/**`、`/media/{id}/file` |
| Portal 公开地图 | `/portal/map/home` |
| Portal 时光合影 | `/portal/seedream/**` |
| 管理端登录/账号 | `/admin/login`、`/admin/register`、`/admin/accounts` |
| 管理端内容 | `/admin/pois`、`/admin/contents`、`/admin/media` |
| 管理端审核/运营 | `/admin/ugc`、`/admin/comments`、`/admin/dashboard`、`/admin/map`、`/admin/logs` |
| 管理端 Agent | `/admin/agent/**` |

详细接口边界见 [../docs/technical-spec.md](../docs/technical-spec.md)。开发期可使用 Swagger UI，团队 API 管理以 ApiFox 为主。

## 配置

主要配置文件：

```text
timecampus-server/src/main/resources/application-example.yaml
timecampus-server/src/main/resources/application-dev-example.yaml
timecampus-server/src/main/resources/application-prod-example.yaml
```

真实 `application-dev.yaml`、`application-prod.yaml` 和密钥不得提交。常用环境变量包括：

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_DATABASE
REDIS_PASSWORD
WECHAT_APPID
WECHAT_SECRET
TENCENT_MAP_KEY
TENCENT_MAP_SK
CAP_ENABLED
CAP_SITEVERIFY_URL
CAP_SECRET
ARK_SEEDREAM_ENABLED
ARK_SEEDREAM_API_KEY
ARK_SEEDREAM_DAILY_IP_LIMIT
TIMECAMPUS_MCP_ENABLED
TIMECAMPUS_MCP_AUTH_REQUIRED
TIMECAMPUS_MCP_TOKEN
TIMECAMPUS_STORAGE_DIR
TIMECAMPUS_MEDIA_FILE_TOKEN_TTL_SECONDS
```

生产 Compose 变量由根仓库 [.env.example](../.env.example) 维护。

## 核心设计

- 用户端和管理端都使用 `Authorization: Bearer <token>`。
- 管理端角色为 `super`、`admin`、`read`、`none`。
- 生产管理端登录必须开启 Cap 服务端校验。
- Portal Seedream 生成接口必须通过 Cap 校验，并对同一 IP 执行每日限额；接口只接受人物图片和白名单背景，不接受自由 prompt。
- 用户端媒体文件 URL 使用短期 accessToken，默认有效期 600 秒。
- 本地文件读取必须位于 `storage.local-root-dir` 下，避免路径穿越。
- `TimeFillAspect` 自动填充 `createTime` 和 `updateTime`。
- 腾讯地图 SK 存在时后端按腾讯规则生成 `sig`。
- RAG 默认支持 BM25 词法检索；启用 Qdrant 后使用 Dense + BM25 + RRF
  混合检索，并按 source 去重向量 chunk。

## MCP 与 RAG

Backend 暴露 Streamable HTTP MCP Server：

```text
POST/GET http://127.0.0.1:8080/mcp
```

生产环境应开启：

```text
TIMECAMPUS_MCP_ENABLED=true
TIMECAMPUS_MCP_AUTH_REQUIRED=true
TIMECAMPUS_MCP_TOKEN=<long-random-token>
```

MCP 提供 POI、影像、RAG、文案维护相关 Tools、Resources 和 Prompts。完整说明见 [docs/mcp-server.md](docs/mcp-server.md)。

## 数据库

初始化脚本：

```text
timecampus-server/src/main/resources/sql/schema.sql
```

当前核心表：

- `user`
- `admin`
- `poi`
- `media`
- `favorite`
- `comment`
- `log`

脚本默认不创建物理外键，关联完整性由应用层校验。详细字段见 [docs/database.md](docs/database.md)。

## 测试

```powershell
mvn test
mvn -pl timecampus-server -am test
```

第三方真实冒烟测试默认跳过，需要显式配置环境变量和真实 key 后运行。测试覆盖统一响应、异常、请求 ID、用户登录、评论、收藏、UGC、文件路径安全、媒体短期 URL、AOP 时间填充、控制器映射、管理端接口、腾讯地图签名和微信响应兼容。

## 部署

生产部署由根仓库 `compose.yaml` 编排，Backend 使用 `prod` profile，在 Compose 网络中连接 Valkey、Qdrant、Ollama、Cap 和挂载存储。完整部署流程见 [../docs/deploy.md](../docs/deploy.md)。

## 相关文档

- 项目功能规格：[../docs/functional-spec.md](../docs/functional-spec.md)
- 项目技术规格：[../docs/technical-spec.md](../docs/technical-spec.md)
- 文档维护指南：[../docs/documentation-maintenance.md](../docs/documentation-maintenance.md)
- MCP Server：[docs/mcp-server.md](docs/mcp-server.md)
- Seedream Image Agent：[docs/seedream-agent.md](docs/seedream-agent.md)
- 数据库设计：[docs/database.md](docs/database.md)
- Alpha Release Notes：[docs/alpha-release-notes.md](docs/alpha-release-notes.md)
- Alpha Test Report：[docs/alpha-test-report.md](docs/alpha-test-report.md)
