# 时光航迹后端

当前交付版本：`0.1.0-alpha`

“时光航迹”（TimeCampus）是面向校园历史影像浏览与共创的小程序后端项目，并为 Web 管理端提供 API。当前实现聚焦 Alpha 阶段最小可交付内容：微信登录、POI 管理、官方内容导入、地图聚合、时间切换、收藏、UGC 上传与审核、审计日志、腾讯地图 WebService 封装、管理端 API。
团队使用 ApiFox 进行 API 文档管理，后续会持续完善接口定义、示例请求与响应、错误码说明等内容。

## 当前状态

- 后端：Spring Boot 3.3.11，Java 21，多模块 Maven 工程。
- 管理端：Vue 3 + Vite + Element Plus，已迁出本后端仓库独立管理。
- API 路径统一为 `/api/v1/...`。
- Alpha 交付状态：核心后端接口、管理端 API、评论审核、运营地图、文件上传、腾讯地图接入、部署文档和测试报告已进入交付前确认。
- 响应格式统一为：

```json
{"code":0,"message":"ok","data":{}}
```

状态码与状态信息定义在 `timecampus-common` 模块的 `ResultCode` 中

## 技术栈

- Java 21
- Spring Boot 3.3.11
- Maven 3.9+
- MyBatis
- MySQL 8
- Redis 7
- Vue 3
- Vite
- Element Plus
- 腾讯地图 WebService API
- 微信小程序 code2Session

## 项目结构

```text
timecampus-backend
├─ timecampus-common       # 通用基础设施：统一响应、错误码、业务异常、全局异常、请求 ID
├─ timecampus-pojo         # DTO / Entity / VO / 模型常量
├─ timecampus-server       # Spring Boot 服务端：Controller / Service / Mapper / 配置
├─ docs                   # 数据库、部署、测试与 Alpha 交付文档
└─ .github/workflows      # CI/CD
```

后端 controller 已按使用端拆分：

```text
timecampus-server/src/main/java/com/notfound/timecampusserver/controller
├─ admin                  # 管理端接口
└─ user                   # 用户端/小程序接口
```

## 后端能力

### 用户端

- 微信小程序登录：`POST /api/v1/auth/wechat/login`
- 当前用户信息：`GET /api/v1/me`
- 当前用户审核结果汇总：`GET /api/v1/me/review-results?status=pending|approved|rejected`
- 当前用户 UGC 审核结果：`GET /api/v1/me/review-results/ugc?status=pending|approved|rejected`
- 当前用户评论审核结果：`GET /api/v1/me/review-results/comments?status=pending|approved|rejected`
- 按用户 ID 查询审核结果汇总：`GET /api/v1/users/{id}/review-results?status=pending|approved|rejected`，仅允许查询本人
- 按用户 ID 查询 UGC / 评论审核结果：`GET /api/v1/users/{id}/review-results/ugc`、`GET /api/v1/users/{id}/review-results/comments`，仅允许查询本人
- POI 公开查询：`GET /api/v1/pois`、`GET /api/v1/pois/{id}`
- 内容查询：`GET /api/v1/pois/{id}/contents`、`GET /api/v1/pois/{id}/official-contents`、`GET /api/v1/contents/{id}`
- 时间切换：`GET /api/v1/pois/{id}/time-switch?year=YYYY`
- 收藏：`POST /api/v1/favorites/{targetType}/{targetId}`、`DELETE /api/v1/favorites/{targetType}/{targetId}`、`GET /api/v1/favorites`
- UGC 上传：`POST /api/v1/ugc`
- 评论：`POST /api/v1/comments`、`GET /api/v1/comments`、`GET /api/v1/my/comments`
- 地图聚合与辅助：`GET /api/v1/map/home`、`GET /api/v1/map/poi/{poiId}/timemachine`、`GET /api/v1/map/reverse-geocode`、`GET /api/v1/map/poi-search`

### 管理端

- 管理员登录/登出：`POST /api/v1/admin/login`、`POST /api/v1/admin/logout`；生产登录请求需要携带 Cap 返回的 `capToken`
- POI 管理：`/api/v1/admin/pois`
- 官方内容批量导入：`POST /api/v1/admin/contents/batch-import`
- 官方影像批量导入：`POST /api/v1/admin/media/import`
- 官方影像上传：`POST /api/v1/admin/media/upload`
- UGC 审核：`GET /api/v1/admin/ugc`、`POST /api/v1/admin/ugc/{id}/approve`、`POST /api/v1/admin/ugc/{id}/reject`
- 评论审核：`GET /api/v1/admin/comments`、`POST /api/v1/admin/comments/{id}/approve`、`POST /api/v1/admin/comments/{id}/reject`
- 审计日志查看：`GET /api/v1/admin/logs`

## 管理端能力

管理端前端已迁出本后端仓库，独立仓库建议使用 `timecampus-ui` 命名。后端当前提供这些管理端 API 能力：

- 登录
- 运营首页
- POI 管理
- 运营地图：查看 POI 点位及收藏、评论、媒体、UGC 概览
- 官方内容列表与批量导入
- 官方影像上传（单文件）
- UGC 审核
- 腾讯地图辅助搜索
- 审计日志查看

前端本地开发时可通过 Vite dev server 代理访问后端：

```text
/api/v1 -> http://localhost:8080/api/v1
```

前端代码中 Axios 的 baseURL 为 `/api/v1`。

## 数据库

建库脚本是数据库结构的真源：

- [`timecampus-server/src/main/resources/sql/schema.sql`](timecampus-server/src/main/resources/sql/schema.sql)

当前核心表：

- `user`
- `admin`
- `poi`
- `media`
- `favorite`
- `comment`
- `log`

为了本地开发和 CI 稳定，脚本中未使用物理外键约束，关联关系由应用层校验。

## 关键设计

### Token 鉴权

- 用户端使用 `Authorization: Bearer <token>`
- 管理端使用 `Authorization: Bearer <token>`
- token 当前存储在 Redis 中
- `/api/v1/admin/**` 需要管理员 token
- `/api/v1/me`、用户审核结果、收藏、UGC 上传等用户行为接口需要用户 token
- 会返回媒体访问 URL 的用户端接口也需要用户 token，包括 `/api/v1/pois/{id}/contents`、`/api/v1/pois/{id}/official-contents`、`/api/v1/contents/{id}`、`/api/v1/pois/{id}/time-switch`、`/api/v1/map/home`、`/api/v1/map/poi/{poiId}/timemachine`、`/api/v1/timeline`

### UGC 审核流程

- 上传后：`review_status = pending`
- 审核通过：`review_status = approved`
- 审核驳回：`review_status = rejected`，必须填写驳回原因

### AOP 时间填充

`timecampus-server` 使用 `TimeFillAspect` 拦截 MyBatis mapper 的 `insert*` / `update*` 方法：

- 新增时自动填充 `createTime`、`updateTime`
- 修改时自动刷新 `updateTime`

Mapper XML 显式写入 `create_time` / `update_time`，避免依赖数据库隐式行为。

### 审计日志

关键写操作会写入 `log` 表：

- POI 新增、修改、删除
- 官方内容批量导入
- UGC 上传
- UGC 通过、驳回

管理端通过 `/api/v1/admin/logs` 查询。

### 文件存储

上传文件默认写入服务器本地挂载路径：

```yaml
storage:
  local-root-dir: ${TIMECAMPUS_STORAGE_DIR:/home/ubuntu/cos}
  max-file-size-mb: 10
  media-file-token-ttl-seconds: 600
```

生产环境建议保持 `/home/ubuntu/cos` 为 COS 挂载或同步目录。数据库中的媒体路径使用绝对路径保存，后端读取文件时会校验路径必须位于 `storage.local-root-dir` 下，以避免相对路径受 Jar 启动目录影响或发生路径穿越。

用户端接口不会直接暴露本地文件路径。`MediaVO.imagePath`、`MediaVO.previewUrl`、地图返回的 `coverImagePath`、`coverPreviewUrl`、`mediaList[].imagePath`、`mediaList[].previewUrl` 均为前端可直接作为图片 `src` 使用的访问 URL。对于本地挂载文件，URL 会携带短期 `accessToken`，例如：

```text
https://api.example.com/api/v1/media/123/file?accessToken=...
```

小程序图片组件不能稳定携带 `Authorization` 请求头，因此媒体文件直出接口使用短期 URL token 鉴权。前端应使用内容接口响应中的 URL，不要自行拼接 `/api/v1/media/{id}/file`。`accessToken` 默认有效期为 600 秒，可通过 `storage.media-file-token-ttl-seconds` 或环境变量 `TIMECAMPUS_MEDIA_FILE_TOKEN_TTL_SECONDS` 调整。

管理端预览仍使用管理员鉴权接口 `/api/v1/admin/media/{id}/file`，不要复用用户端短期 URL 规则。

### 腾讯地图 Sig

腾讯地图配置支持 Key + SK：

```yaml
tencent-map:
  key: ${TENCENT_MAP_KEY:}
  sk: ${TENCENT_MAP_SK:}
```

当 `sk` 为空时，后端使用普通 Key 请求；当 `sk` 存在时，后端会根据腾讯位置服务规则按参数名排序、拼接原始参数并追加 `sig`。签名逻辑位于 `TencentMapSignature`，并覆盖了单元测试。

## 配置项

主要配置文件：

- `timecampus-server/src/main/resources/application.yaml`
- `timecampus-server/src/main/resources/application-example.yaml`
- `timecampus-server/src/main/resources/application-dev-example.yaml`
- `timecampus-server/src/main/resources/application-prod-example.yaml`

`application-dev.yaml` 和 `application-prod.yaml` 包含数据库密码、Redis 密码、微信密钥、腾讯地图 SK 等敏感信息，已取消版本管理。首次本地运行时请从示例文件复制：

```shell
copy timecampus-server\src\main\resources\application-dev-example.yaml timecampus-server\src\main\resources\application-dev.yaml
```

Linux/macOS：

```shell
cp timecampus-server/src/main/resources/application-dev-example.yaml timecampus-server/src/main/resources/application-dev.yaml
```

然后在本机的 `application-dev.yaml` 或环境变量中填入真实值。不要提交 `application-dev.yaml`。

生产服务器使用 `prod` profile，推荐在服务器 `~/TimeCampus-Backend/app/config/application-prod.yaml` 放置真实配置，并从 `application-prod-example.yaml` 复制后修改。Spring Boot 启动命令为：

```shell
java -jar app.jar --spring.profiles.active=prod
```

### 最小运行配置

后端启动所需最小配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/timecampus?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: your-db-password
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: your-redis-password
```

如需微信登录，需要配置：

```yaml
wechat:
  appid: your-wechat-appid
  secret: your-wechat-secret
```

如需腾讯地图接口，需要配置：

```yaml
tencent-map:
  key: your-tencent-key
  sk: your-tencent-sk
```

生产管理端登录需要开启 Cap 后端校验：

```yaml
cap:
  enabled: true
  siteverify-url: https://cap.timecampus.asia/<site-key>/siteverify
  secret: your-cap-site-secret
```

本地开发可使用 `CAP_ENABLED=false` 跳过验证码校验；生产必须使用 `CAP_ENABLED=true`，且前端不能保存或暴露 `CAP_SECRET`。

也可以通过环境变量注入敏感信息：

```text
DB_USERNAME
DB_PASSWORD
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
TIMECAMPUS_MEDIA_FILE_TOKEN_TTL_SECONDS
```

本地开发需要 MySQL 和 Redis 可用。

## 快速启动

### 后端

```shell
mvn clean test
mvn -pl timecampus-server -am spring-boot:run
```

服务默认端口：

```text
http://localhost:8080
```

Swagger UI：

```text
http://localhost:8080/swagger-ui/index.html
```

### 管理端

前端已迁出本仓库。请在独立的 `timecampus-ui` 仓库中运行：

```shell
cd timecampus-ui
npm install
npm run dev
```

默认地址：

```text
http://localhost:8081
```

构建：

```shell
npm run build
```

## 测试

默认测试：

```shell
mvn test
```

当前覆盖内容包括：

- common 统一响应、异常处理、请求 ID
- pojo DTO 校验与实体字段契约
- 用户登录服务
- 评论创建、查询、审核与统一响应回归
- 收藏逻辑
- UGC 上传与审核规则
- 文件上传校验、绝对路径存储、媒体文件访问路径安全
- 媒体文件短期 URL token 鉴权
- AOP 时间填充
- `/api/v1` 回归接口
- 管理端日志接口
- 腾讯地图 Sig 签名
- 微信 code2Session 文本响应兼容

第三方真实冒烟测试默认跳过，需显式开启。

腾讯地图冒烟测试：

```shell
set RUN_TENCENT_SMOKE=true
set TENCENT_MAP_KEY=your-key
set TENCENT_MAP_SK=your-sk
mvn -pl timecampus-server -am "-Dtest=com.notfound.timecampusserver.smoke.TencentMapSmokeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

微信冒烟测试：

```shell
set RUN_WECHAT_SMOKE=true
set WECHAT_APPID=your-appid
set WECHAT_SECRET=your-secret
mvn -pl timecampus-server -am "-Dtest=com.notfound.timecampusserver.smoke.WechatAuthSmokeTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

## CI/CD

- CI：`.github/workflows/ci.yml`
  - 执行 `mvn clean test`
  - 打包服务模块
  - 上传构建产物
- CD：`.github/workflows/cd.yml`
  - 触发条件：向 `main` 发起 PR 且来源分支为 `release`，或手动（`workflow_dispatch`）触发
  - 不会在向 `main` 直接 push 时自动运行
  - 服务器侧执行 `git pull origin main` 后本地构建并以 `prod` profile 运行

## Alpha 交付文档

- [`docs/alpha-release-notes.md`](docs/alpha-release-notes.md)：Alpha 版本范围、已知限制与交付检查清单。
- [`docs/alpha-test-report.md`](docs/alpha-test-report.md)：测试计划、测试过程、测试矩阵、压测结果与 Alpha 出口条件。

## 分支策略

- `main`：稳定分支
- `release`：预发布分支
- `dev`：日常集成分支
- `feature/*`、`fix/*`、`chore/*`：功能、修复、维护分支

## 未来迭代方向

### 认证与权限

- 将当前 Redis token 升级为更完整的 JWT + Refresh Token 或统一会话模型。
- 增加登录失败次数限制、验证码、管理员密码重置流程。

### 数据模型

### 内容与 UGC

- 增加内容标签体系，支持官方内容和 UGC 的多维度标签分类。
- 增加内容版本管理，支持内容的历史版本回滚和差异对比。
- UGC 增加编辑功能，允许用户修改待审核或已驳回的内容，并重新提交审核。
- 增加内容推荐算法，基于用户行为和内容特征进行个性化推荐。
- 增加举报和并引入自动审核机制。

### 存储能力

### 地图能力

- 增加腾讯地图 API 响应缓存，降低配额消耗。
- 对腾讯地图 API status 做统一错误映射。
- 支持地点搜索结果一键转 POI 草稿。
- 增加地图坐标合法性和边界校验工具。

### 管理端页面优化

- 完善分页、排序、批量操作。
- 增加用户管理、评论审核、日志详情页。
- 接入 ECharts，做内容增长、审核效率、POI 热度等运营看板。

### 测试与质量

### API 文档

- 使用 ApiFox 平台进行管理
- https://localhost:8080/swagger-ui/index.html 仅供开发阶段使用。
